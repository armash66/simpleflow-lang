import { exec } from 'child_process';
import { promises as fs } from 'fs';
import path from 'path';
import { tmpdir } from 'os';
import { randomBytes } from 'crypto';

export default async function handler(req, res) {
    if (req.method !== 'POST') {
        return res.status(405).json({ error: 'Method not allowed' });
    }

    const { code } = req.body;
    if (!code) {
        return res.status(400).json({ error: 'No code provided' });
    }

    const fileId = randomBytes(8).toString('hex');
    const tempFilePath = path.join(tmpdir(), `${fileId}.sf`);

    try {
        await fs.writeFile(tempFilePath, code);

        // If Vercel env variable exists, use its dynamically downloaded Linux java path, otherwise use system java
        const javaPath = process.env.VERCEL ? path.join(process.cwd(), 'jre', 'bin', 'java') : 'java';
        const jarPath = path.join(process.cwd(), 'simpleflow-lang', 'simpleflow-lang.jar');

        const result = await new Promise((resolve) => {
            exec(`"${javaPath}" -cp "${jarPath}" com.simpleflow.lang.Main "${tempFilePath}"`, { timeout: 3000 }, (error, stdout, stderr) => {
                resolve({ error, stdout, stderr });
            });
        });

        let output = result.stdout || '';
        if (output) {
            output = output.replace(/\r\n/g, '\n').trim();
        }

        let errorStr = result.stderr || '';
        if (result.error && !errorStr) {
            if (result.error.killed) {
                errorStr = 'Execution timed out (3000ms)';
            } else {
                errorStr = result.error.message;
            }
        }

        res.status(200).json({ output, error: errorStr.trim() });

    } catch (err) {
        res.status(500).json({ error: err.message });
    } finally {
        try {
            await fs.unlink(tempFilePath);
        } catch (e) { }
    }
}

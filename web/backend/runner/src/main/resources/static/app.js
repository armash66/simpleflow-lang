const editor = document.getElementById("codeEditor");
const output = document.getElementById("output");
const errors = document.getElementById("errors");
const runBtn = document.getElementById("runBtn");
const clearBtn = document.getElementById("clearBtn");
const runMeta = document.getElementById("runMeta");
const editorMeta = document.getElementById("editorMeta");
const outTab = document.getElementById("outTab");
const errTab = document.getElementById("errTab");
const menuBtn = document.getElementById("menuBtn");
const sidebar = document.getElementById("sidebar");
const overlay = document.getElementById("overlay");
const navLinks = document.querySelectorAll(".nav-link");
const programName = document.getElementById("programName");
const saveProgramBtn = document.getElementById("saveProgramBtn");
const programList = document.getElementById("programList");

const STORAGE_KEY = "simpleflow.editor";
const LIBRARY_KEY = "simpleflow.library";
let currentController = null;

const defaultCode = `store a = 10\nstore b = 3\n\nprint "a+b="\nshow a + b\n\nwhen (a > 10) {\n  show "big"\n} otherwise when (a > 5 and b < 5) {\n  show "medium"\n} otherwise {\n  show "small"\n}\n\nloop (store i = 1; i <= 3; i++) {\n  show i\n}\n\nstore c = @(1, 2, null, 4)\nshow c[2] == null\n\nexit`;

function setStatus(text) {
  runMeta.textContent = text;
}

function setOutput(text) {
  output.textContent = text || "";
}

function setError(text) {
  errors.textContent = text || "";
}

function switchTab(tab) {
  const showErrors = tab === "errors";
  errors.classList.toggle("hidden", !showErrors);
  output.classList.toggle("hidden", showErrors);
  errTab.classList.toggle("active", showErrors);
  outTab.classList.toggle("active", !showErrors);
}

function loadInitialCode() {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (saved) {
    editor.value = saved;
    return;
  }
  editor.value = defaultCode;
}

function saveCode() {
  localStorage.setItem(STORAGE_KEY, editor.value);
  editorMeta.textContent = "Autosaved";
  setTimeout(() => {
    editorMeta.textContent = "Autosave on";
  }, 800);
}

function loadLibrary() {
  try {
    const raw = localStorage.getItem(LIBRARY_KEY);
    if (!raw) return [];
    const data = JSON.parse(raw);
    if (!Array.isArray(data)) return [];
    return data;
  } catch {
    return [];
  }
}

function saveLibrary(list) {
  localStorage.setItem(LIBRARY_KEY, JSON.stringify(list));
}

function renderLibrary() {
  if (!programList) return;
  const list = loadLibrary();
  programList.innerHTML = "";

  if (list.length === 0) {
    const empty = document.createElement("div");
    empty.className = "panel-meta";
    empty.textContent = "No programs yet. Save one from the editor.";
    programList.appendChild(empty);
    return;
  }

  list.forEach((item, index) => {
    const row = document.createElement("div");
    row.className = "program-item";

    const name = document.createElement("div");
    name.className = "program-name";
    name.textContent = item.name;

    const loadBtn = document.createElement("button");
    loadBtn.className = "btn small";
    loadBtn.textContent = "Load";
    loadBtn.addEventListener("click", () => {
      editor.value = item.code;
      saveCode();
      setActivePage("home");
    });

    const delBtn = document.createElement("button");
    delBtn.className = "btn small ghost";
    delBtn.textContent = "Delete";
    delBtn.addEventListener("click", () => {
      const updated = loadLibrary().filter((_, i) => i !== index);
      saveLibrary(updated);
      renderLibrary();
    });

    row.appendChild(name);
    row.appendChild(loadBtn);
    row.appendChild(delBtn);
    programList.appendChild(row);
  });
}

function setActivePage(page) {
  document.querySelectorAll(".page-section").forEach((section) => {
    section.classList.toggle("active", section.id === `page-${page}`);
  });
  navLinks.forEach((link) => {
    link.classList.toggle("active", link.dataset.page === page);
  });
  closeSidebar();
}

function openSidebar() {
  sidebar.classList.add("open");
  overlay.classList.add("show");
  menuBtn.setAttribute("aria-expanded", "true");
}

function closeSidebar() {
  sidebar.classList.remove("open");
  overlay.classList.remove("show");
  menuBtn.setAttribute("aria-expanded", "false");
}

async function runCode() {
  if (currentController) {
    currentController.abort();
  }

  setStatus("Running...");
  setError("");
  switchTab("output");

  const start = performance.now();
  currentController = new AbortController();

  try {
    const response = await fetch("/run", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code: editor.value }),
      signal: currentController.signal
    });

    const data = await response.json();
    const elapsed = Math.round(performance.now() - start);

    if (data.error) {
      setError(data.error);
      switchTab("errors");
      setStatus(`Error in ${elapsed}ms`);
      return;
    }

    setOutput(data.output);
    setStatus(`Completed in ${elapsed}ms`);
  } catch (err) {
    if (err.name === "AbortError") {
      setStatus("Stopped");
      return;
    }
    setError("Failed to reach runner");
    switchTab("errors");
    setStatus("Error");
  } finally {
    currentController = null;
  }
}

function clearOutput() {
  setOutput("");
  setError("");
  setStatus("Cleared");
}

editor.addEventListener("input", () => {
  saveCode();
});

runBtn.addEventListener("click", runCode);
clearBtn.addEventListener("click", clearOutput);

if (saveProgramBtn) {
  saveProgramBtn.addEventListener("click", () => {
    const name = programName.value.trim();
    if (!name) {
      programName.focus();
      return;
    }
    const list = loadLibrary();
    const existingIndex = list.findIndex((item) => item.name === name);
    const entry = { name, code: editor.value };
    if (existingIndex >= 0) {
      list[existingIndex] = entry;
    } else {
      list.unshift(entry);
    }
    saveLibrary(list);
    programName.value = "";
    renderLibrary();
  });
}

outTab.addEventListener("click", () => switchTab("output"));
errTab.addEventListener("click", () => switchTab("errors"));

navLinks.forEach((link) => {
  link.addEventListener("click", () => {
    setActivePage(link.dataset.page);
  });
});

menuBtn.addEventListener("click", () => {
  const isOpen = sidebar.classList.contains("open");
  if (isOpen) {
    closeSidebar();
  } else {
    openSidebar();
  }
});

overlay.addEventListener("click", closeSidebar);

window.addEventListener("keydown", (event) => {
  if ((event.ctrlKey || event.metaKey) && event.key === "Enter") {
    event.preventDefault();
    runCode();
  }
  if (event.key === "Escape") {
    closeSidebar();
  }
});

loadInitialCode();
renderLibrary();
setStatus("Idle");

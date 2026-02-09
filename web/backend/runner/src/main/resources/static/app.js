const editor = document.getElementById("codeEditor");
const lineNumbers = document.getElementById("lineNumbers");
const lineNumbersInner = document.getElementById("lineNumbersInner");
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

const defaultCode = `# SimpleFlow: Maze Solver (BFS)

define makeGrid(rows, cols) {
  store grid = @()
  store r = 1
  while (r <= rows) {
    store row = @()
    store c = 1
    while (c <= cols) {
      row[c] = "."
      c++
    }
    grid[r] = row
    r++
  }
  return grid
}

define printGrid(grid) {
  store r = 1
  while (r <= length(grid)) {
    store line = ""
    store c = 1
    while (c <= length(grid[r])) {
      line = line + grid[r][c]
      c++
    }
    show line
    r++
  }
}

define inBounds(r, c, rows, cols) {
  return r >= 1 and r <= rows and c >= 1 and c <= cols
}

define setCell(grid, r, c, value) {
  store row = grid[r]
  row[c] = value
  grid[r] = row
}

define enqueue(q, item) {
  q[length(q) + 1] = item
}

define dequeue(q) {
  store item = q[1]
  # shift left
  store i = 1
  while (i < length(q)) {
    q[i] = q[i + 1]
    i++
  }
  # remove last
  q[length(q)] = null
  return item
}

define queueEmpty(q) {
  return length(q) == 0 or q[1] == null
}

# Build maze
store rows = 10
store cols = 18
store maze = makeGrid(rows, cols)

# Walls
store w = @(
  @(2,2), @(2,3), @(2,4), @(2,5), @(2,6),
  @(4,7), @(5,7), @(6,7), @(7,7),
  @(8,3), @(8,4), @(8,5), @(8,6), @(8,7),
  @(3,12), @(4,12), @(5,12), @(6,12), @(7,12), @(8,12)
)

store i = 1
while (i <= length(w)) {
  store r = w[i][1]
  store c = w[i][2]
  setCell(maze, r, c, "#")
  i++
}

# Start / End
store start = @(2, 2)
store end = @(9, 16)
setCell(maze, start[1], start[2], "S")
setCell(maze, end[1], end[2], "E")

# BFS setup
store q = @()
store visited = @()
store prev = @()

# visited and prev are maps: key = "r,c"
define key(r, c) { return r + "," + c }

enqueue(q, start)
visited[key(start[1], start[2])] = true

store found = false

while (not queueEmpty(q)) {
  store cur = dequeue(q)
  store r = cur[1]
  store c = cur[2]

  when (r == end[1] and c == end[2]) {
    found = true
    leave
  }

  # neighbors: up, right, down, left
  store dr = @(-1, 0, 1, 0)
  store dc = @(0, 1, 0, -1)

  store d = 1
  while (d <= 4) {
    store nr = r + dr[d]
    store nc = c + dc[d]

    when (inBounds(nr, nc, rows, cols)) {
      when (maze[nr][nc] != "#") {
        store k = key(nr, nc)
        when (visited[k] != true) {
          visited[k] = true
          prev[k] = @(r, c)
          enqueue(q, @(nr, nc))
        }
      }
    }

    d++
  }
}

# Reconstruct path
when (found) {
  store curR = end[1]
  store curC = end[2]
  while (not (curR == start[1] and curC == start[2])) {
    store k = key(curR, curC)
    store p = prev[k]
    when (p == null) { leave }
    curR = p[1]
    curC = p[2]
    when (maze[curR][curC] == ".") {
      setCell(maze, curR, curC, "*")
    }
  }
}

show "=== Maze ==="
printGrid(maze)

exit`;

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

function updateLineNumbers() {
  if (!lineNumbers) return;
  const lines = editor.value.length === 0 ? 1 : editor.value.split("\n").length;
  let output = "";
  for (let i = 1; i <= lines; i++) {
    output += i + "\n";
  }
  if (lineNumbersInner) {
    lineNumbersInner.textContent = output;
    lineNumbersInner.style.transform = `translateY(-${editor.scrollTop}px)`;
  } else {
    lineNumbers.textContent = output;
    lineNumbers.scrollTop = editor.scrollTop;
  }
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
  updateLineNumbers();
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

editor.addEventListener("scroll", () => {
  if (lineNumbersInner) {
    lineNumbersInner.style.transform = `translateY(-${editor.scrollTop}px)`;
  } else if (lineNumbers) {
    lineNumbers.scrollTop = editor.scrollTop;
  }
});

window.addEventListener("resize", updateLineNumbers);

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
updateLineNumbers();
renderLibrary();
setStatus("Idle");

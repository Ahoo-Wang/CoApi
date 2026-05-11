# AGENTS.md — Wiki (VitePress)

## Build & Run Commands

```bash
cd wiki
npm install           # Install dependencies
npm run dev           # Dev server at localhost:5173
npm run build         # Production build to wiki/.vitepress/dist
npm run preview       # Preview production build
```

## Wiki Structure

```
wiki/
├── index.md                    # Landing page
├── getting-started/            # Overview, installation, quick-start, config
├── deep-dive/                  # Architecture, annotations, modes, LB, auth, customization, examples
├── onboarding/                 # Contributor, staff engineer, executive, PM guides
├── llms.txt                    # LLM-friendly summary (wiki-relative)
├── llms-full.txt               # Full page content inlined
└── .vitepress/
    ├── config.mjs              # VitePress config with Mermaid plugin
    └── theme/
        ├── index.js            # Theme with medium-zoom
        └── custom.css          # Daytona dark theme + Mermaid dark overrides
```

## Content Conventions

- **Mermaid diagrams**: Use dark-mode colors (fills `#2d333b`, borders `#6d5dfc`, text `#e6edf3`)
- **Citation format**: `[file_path:line_number](https://github.com/Ahoo-Wang/CoApi/blob/main/file_path#Lline_number)`
- **Frontmatter**: Every page must have `title` and `description`
- **No `<br/>` in Mermaid**: Use `<br>` or line breaks only
- **autonumber**: Required in all `sequenceDiagram` blocks

## Documentation

- See `wiki/llms.txt` for structured project summary
- See `wiki/llms-full.txt` for complete page content

## Boundaries

- ✅ Update page content and add new pages
- ✅ Update sidebar in `.vitepress/config.mjs`
- ⚠️ Modifying theme CSS requires visual verification
- 🚫 Do not delete generated pages without checking cross-references
- 🚫 Do not modify `.vitepress/config.mjs` build settings without testing

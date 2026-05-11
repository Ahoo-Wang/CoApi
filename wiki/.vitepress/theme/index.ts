import { h, nextTick, watch } from 'vue'
import type { Theme } from 'vitepress'
import DefaultTheme from 'vitepress/theme'
import { useData } from 'vitepress'
import { createMermaidRenderer } from 'vitepress-mermaid-renderer'
import './custom.css'

const toolbarLocales = {
  en: {
    tooltips: {
      zoomIn: 'Zoom In',
      zoomOut: 'Zoom Out',
      resetView: 'Reset View',
      copyCode: 'Copy Code',
      copyCodeCopied: 'Copied',
      download: 'Download Diagram',
      toggleFullscreen: 'Toggle Fullscreen',
    },
  },
}

export default {
  extends: DefaultTheme,
  Layout: () => {
    const { isDark, localeIndex } = useData()

    const initMermaid = () => {
      const mermaidRenderer = createMermaidRenderer({
        theme: isDark.value ? 'dark' : 'default',
      })
      mermaidRenderer.setToolbar({
        showLanguageLabel: false,
        downloadFormat: 'svg',
        desktop: {
          copyCode: 'enabled',
          toggleFullscreen: 'enabled',
          resetView: 'enabled',
          zoomOut: 'enabled',
          zoomIn: 'enabled',
          zoomLevel: 'enabled',
          download: 'enabled',
        },
        fullscreen: {
          copyCode: 'disabled',
          toggleFullscreen: 'enabled',
          resetView: 'disabled',
          zoomLevel: 'disabled',
          download: 'enabled',
        },
        i18n: {
          localeIndex: localeIndex.value,
          locales: toolbarLocales,
        },
      })
    }

    nextTick(() => initMermaid())

    watch(
      () => [isDark.value, localeIndex.value] as const,
      () => {
        initMermaid()
      },
    )

    return h(DefaultTheme.Layout)
  },
} satisfies Theme

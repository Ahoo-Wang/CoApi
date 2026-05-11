import { defineConfig } from 'vitepress'

export default defineConfig({
  lang: 'en-US',
  title: 'CoApi Wiki',
  description: 'Zero-boilerplate HTTP client auto-configuration for Spring 6',
  ignoreDeadLinks: true,
  cleanUrls: true,
  head: [
    ['link', { rel: 'preconnect', href: 'https://fonts.googleapis.com' }],
    ['link', { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' }],
    ['link', { href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap', rel: 'stylesheet' }]
  ],
  themeConfig: {
    logo: '/logo.svg',
    nav: [
      { text: 'Getting Started', link: '/getting-started/overview' },
      { text: 'Deep Dive', link: '/deep-dive/architecture' },
      { text: 'Onboarding', link: '/onboarding/contributor-guide' }
    ],
    sidebar: {
      '/onboarding/': [
        {
          text: 'Onboarding',
          collapsed: false,
          items: [
            { text: 'Contributor Guide', link: '/onboarding/contributor-guide' },
            { text: 'Staff Engineer Guide', link: '/onboarding/staff-engineer-guide' },
            { text: 'Executive Guide', link: '/onboarding/executive-guide' },
            { text: 'Product Manager Guide', link: '/onboarding/product-manager-guide' }
          ]
        }
      ],
      '/getting-started/': [
        {
          text: 'Getting Started',
          items: [
            { text: 'What is CoApi?', link: '/getting-started/overview' },
            { text: 'Installation & Setup', link: '/getting-started/installation' },
            { text: 'Quick Start', link: '/getting-started/quick-start' },
            { text: 'Configuration Reference', link: '/getting-started/configuration' }
          ]
        }
      ],
      '/deep-dive/': [
        {
          text: 'Deep Dive',
          items: [
            { text: 'Architecture Overview', link: '/deep-dive/architecture' },
            { text: 'Annotations', link: '/deep-dive/annotations' },
            { text: 'Client Modes', link: '/deep-dive/client-modes' },
            { text: 'Load Balancing', link: '/deep-dive/load-balancing' },
            { text: 'Customization', link: '/deep-dive/customization' },
            { text: 'Authentication', link: '/deep-dive/authentication' },
            { text: 'Auto-Configuration', link: '/deep-dive/auto-configuration' },
            { text: 'Examples & Patterns', link: '/deep-dive/examples' }
          ]
        }
      ],
      '/': [
        {
          text: 'Onboarding',
          collapsed: false,
          items: [
            { text: 'Contributor Guide', link: '/onboarding/contributor-guide' },
            { text: 'Staff Engineer Guide', link: '/onboarding/staff-engineer-guide' },
            { text: 'Executive Guide', link: '/onboarding/executive-guide' },
            { text: 'Product Manager Guide', link: '/onboarding/product-manager-guide' }
          ]
        },
        {
          text: 'Getting Started',
          collapsed: true,
          items: [
            { text: 'What is CoApi?', link: '/getting-started/overview' },
            { text: 'Installation & Setup', link: '/getting-started/installation' },
            { text: 'Quick Start', link: '/getting-started/quick-start' },
            { text: 'Configuration Reference', link: '/getting-started/configuration' }
          ]
        },
        {
          text: 'Deep Dive',
          collapsed: true,
          items: [
            { text: 'Architecture Overview', link: '/deep-dive/architecture' },
            { text: 'Annotations', link: '/deep-dive/annotations' },
            { text: 'Client Modes', link: '/deep-dive/client-modes' },
            { text: 'Load Balancing', link: '/deep-dive/load-balancing' },
            { text: 'Customization', link: '/deep-dive/customization' },
            { text: 'Authentication', link: '/deep-dive/authentication' },
            { text: 'Auto-Configuration', link: '/deep-dive/auto-configuration' },
            { text: 'Examples & Patterns', link: '/deep-dive/examples' }
          ]
        }
      ]
    },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/Ahoo-Wang/CoApi' }
    ],
    search: {
      provider: 'local'
    },
    editLink: {
      pattern: 'https://github.com/Ahoo-Wang/CoApi/edit/main/wiki/:path',
      text: 'Edit this page on GitHub'
    }
  }
})

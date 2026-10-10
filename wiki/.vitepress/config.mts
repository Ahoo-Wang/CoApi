import { defineConfig } from 'vitepress'

function sidebar(prefix: string, t: Record<string, string>) {
  return [
    {
      text: t.gettingStarted,
      items: [
        { text: t.overview, link: `${prefix}/getting-started/overview` },
        { text: t.installation, link: `${prefix}/getting-started/installation` },
        { text: t.quickStart, link: `${prefix}/getting-started/quick-start` }
      ]
    },
    {
      text: t.guide,
      items: [
        { text: t.annotations, link: `${prefix}/deep-dive/annotations` },
        { text: t.registering, link: `${prefix}/deep-dive/auto-configuration` },
        { text: t.clientModes, link: `${prefix}/deep-dive/client-modes` },
        { text: t.loadBalancing, link: `${prefix}/deep-dive/load-balancing` },
        { text: t.customization, link: `${prefix}/deep-dive/customization` },
        { text: t.authentication, link: `${prefix}/deep-dive/authentication` },
        { text: t.examples, link: `${prefix}/deep-dive/examples` }
      ]
    },
    {
      text: t.reference,
      items: [
        { text: t.configuration, link: `${prefix}/getting-started/configuration` },
        { text: t.troubleshooting, link: `${prefix}/getting-started/troubleshooting` },
        { text: t.migration, link: `${prefix}/getting-started/migration-v3` },
        { text: t.architecture, link: `${prefix}/deep-dive/architecture` }
      ]
    }
  ]
}

const en = {
  gettingStarted: 'Getting Started',
  overview: 'What is CoApi?',
  installation: 'Installation',
  quickStart: 'Quick Start',
  guide: 'Guide',
  annotations: 'Defining Clients',
  registering: 'Registering Clients',
  clientModes: 'Client Modes',
  loadBalancing: 'Load Balancing',
  customization: 'Customization',
  authentication: 'Authentication',
  examples: 'Examples',
  reference: 'Reference',
  configuration: 'Configuration',
  troubleshooting: 'Troubleshooting',
  migration: 'Migrating to 3.0',
  architecture: 'Architecture'
}

const zh = {
  gettingStarted: '开始',
  overview: '什么是 CoApi？',
  installation: '安装',
  quickStart: '快速入门',
  guide: '指南',
  annotations: '定义客户端',
  registering: '注册客户端',
  clientModes: '客户端模式',
  loadBalancing: '负载均衡',
  customization: '自定义',
  authentication: '认证',
  examples: '示例',
  reference: '参考',
  configuration: '配置参考',
  troubleshooting: '故障排查',
  migration: '迁移到 3.0',
  architecture: '架构'
}

const navbarEn = [
  { text: 'Guide', link: '/getting-started/quick-start' },
  { text: 'Reference', link: '/getting-started/configuration' },
  { text: 'Releases', link: 'https://github.com/Ahoo-Wang/CoApi/releases' }
]

const navbarZh = [
  { text: '指南', link: '/zh/getting-started/quick-start' },
  { text: '参考', link: '/zh/getting-started/configuration' },
  { text: '版本发布', link: 'https://github.com/Ahoo-Wang/CoApi/releases' }
]

const sidebarEn = sidebar('', en)
const sidebarZh = sidebar('/zh', zh)

export default defineConfig({
  title: 'CoApi Wiki',
  description: 'Zero-boilerplate auto-configuration for Spring HTTP Interface clients, reactive and synchronous',
  cleanUrls: true,
  srcExclude: ['AGENTS.md', 'CLAUDE.md'],
  sitemap: {
    hostname: 'https://coapi.ahoo.me'
  },
  head: [
    ['link', { rel: 'preconnect', href: 'https://fonts.googleapis.com' }],
    ['link', { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' }],
    ['link', { href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap', rel: 'stylesheet' }],
    ['script', { async: '', src: 'https://www.googletagmanager.com/gtag/js?id=G-3WF6RY5MTT' }],
    ['script', {}, `window.dataLayer = window.dataLayer || [];function gtag(){dataLayer.push(arguments);}gtag('js', new Date());gtag('config', 'G-3WF6RY5MTT');`]
  ],
  themeConfig: {
    socialLinks: [
      { icon: 'github', link: 'https://github.com/Ahoo-Wang/CoApi' }
    ],
    editLink: {
      pattern: 'https://github.com/Ahoo-Wang/CoApi/edit/main/wiki/:path'
    },
    search: {
      provider: 'local'
    }
  },
  locales: {
    root: {
      label: 'English',
      lang: 'en-US',
      link: '/',
      themeConfig: {
        nav: navbarEn,
        sidebar: sidebarEn,
        lastUpdated: {
          text: 'Last updated'
        },
        outline: {
          label: 'On this page',
          level: [2, 3]
        },
        notFound: {
          title: 'Page Not Found',
          quote: 'The page you are looking for does not exist.',
          linkText: 'Go home'
        },
        editLink: {
          text: 'Edit this page on GitHub'
        },
        search: {
          options: {
            translations: {
              button: {
                buttonText: 'Search',
                buttonAriaLabel: 'Search'
              },
              modal: {
                noResultsText: 'No results for',
                resetButtonTitle: 'Clear search query',
                footer: {
                  selectText: 'to select',
                  navigateText: 'to navigate',
                  closeText: 'to close'
                }
              }
            }
          }
        }
      }
    },
    zh: {
      label: '中文',
      lang: 'zh-CN',
      link: '/zh/',
      themeConfig: {
        nav: navbarZh,
        sidebar: sidebarZh,
        lastUpdated: {
          text: '上次更新'
        },
        outline: {
          label: '本页目录',
          level: [2, 3]
        },
        notFound: {
          title: '页面未找到',
          quote: '你访问的页面不存在。',
          linkText: '返回首页'
        },
        editLink: {
          text: '在 GitHub 上编辑此页'
        },
        search: {
          options: {
            translations: {
              button: {
                buttonText: '搜索',
                buttonAriaLabel: '搜索'
              },
              modal: {
                noResultsText: '未找到结果',
                resetButtonTitle: '清除搜索查询',
                footer: {
                  selectText: '选择',
                  navigateText: '导航',
                  closeText: '关闭'
                }
              }
            }
          }
        }
      }
    }
  }
})

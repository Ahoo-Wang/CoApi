import { defineConfig } from 'vitepress'

const navbarEn = [
  { text: 'Getting Started', link: '/getting-started/overview' },
  { text: 'Deep Dive', link: '/deep-dive/architecture' },
  { text: 'Onboarding', link: '/onboarding/contributor-guide' }
]

const sidebarEn = {
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
  ]
}

const navbarZh = [
  { text: '快速开始', link: '/zh/getting-started/overview' },
  { text: '深入了解', link: '/zh/deep-dive/architecture' },
  { text: '新手指南', link: '/zh/onboarding/contributor-guide' }
]

const sidebarZh = {
  '/zh/onboarding/': [
    {
      text: '新手指南',
      collapsed: false,
      items: [
        { text: '贡献者指南', link: '/zh/onboarding/contributor-guide' },
        { text: '技术专家指南', link: '/zh/onboarding/staff-engineer-guide' },
        { text: '管理者指南', link: '/zh/onboarding/executive-guide' },
        { text: '产品经理指南', link: '/zh/onboarding/product-manager-guide' }
      ]
    }
  ],
  '/zh/getting-started/': [
    {
      text: '快速开始',
      items: [
        { text: '什么是 CoApi？', link: '/zh/getting-started/overview' },
        { text: '安装与配置', link: '/zh/getting-started/installation' },
        { text: '快速入门', link: '/zh/getting-started/quick-start' },
        { text: '配置参考', link: '/zh/getting-started/configuration' }
      ]
    }
  ],
  '/zh/deep-dive/': [
    {
      text: '深入了解',
      items: [
        { text: '架构概述', link: '/zh/deep-dive/architecture' },
        { text: '注解说明', link: '/zh/deep-dive/annotations' },
        { text: '客户端模式', link: '/zh/deep-dive/client-modes' },
        { text: '负载均衡', link: '/zh/deep-dive/load-balancing' },
        { text: '自定义配置', link: '/zh/deep-dive/customization' },
        { text: '认证授权', link: '/zh/deep-dive/authentication' },
        { text: '自动配置', link: '/zh/deep-dive/auto-configuration' },
        { text: '示例与模式', link: '/zh/deep-dive/examples' }
      ]
    }
  ],
  '/zh/': [
    {
      text: '新手指南',
      collapsed: false,
      items: [
        { text: '贡献者指南', link: '/zh/onboarding/contributor-guide' },
        { text: '技术专家指南', link: '/zh/onboarding/staff-engineer-guide' },
        { text: '管理者指南', link: '/zh/onboarding/executive-guide' },
        { text: '产品经理指南', link: '/zh/onboarding/product-manager-guide' }
      ]
    },
    {
      text: '快速开始',
      collapsed: true,
      items: [
        { text: '什么是 CoApi？', link: '/zh/getting-started/overview' },
        { text: '安装与配置', link: '/zh/getting-started/installation' },
        { text: '快速入门', link: '/zh/getting-started/quick-start' },
        { text: '配置参考', link: '/zh/getting-started/configuration' }
      ]
    },
    {
      text: '深入了解',
      collapsed: true,
      items: [
        { text: '架构概述', link: '/zh/deep-dive/architecture' },
        { text: '注解说明', link: '/zh/deep-dive/annotations' },
        { text: '客户端模式', link: '/zh/deep-dive/client-modes' },
        { text: '负载均衡', link: '/zh/deep-dive/load-balancing' },
        { text: '自定义配置', link: '/zh/deep-dive/customization' },
        { text: '认证授权', link: '/zh/deep-dive/authentication' },
        { text: '自动配置', link: '/zh/deep-dive/auto-configuration' },
        { text: '示例与模式', link: '/zh/deep-dive/examples' }
      ]
    }
  ]
}

export default defineConfig({
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

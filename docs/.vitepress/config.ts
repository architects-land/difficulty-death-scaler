import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Difficulty Death Scaler",
  description: "Official wiki of Difficulty Death Scaler",
  base: '/difficulty-death-scaler/',
  lang: 'en-US',
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Installation', link: '/installation' },
      { text: 'Global difficulty', link: '/difficulty/global' },
      { text: 'Player difficulty', link: '/difficulty/player' },
    ],

    sidebar: [
      {
        text: 'Introduction',
        items: [
          { text: 'Presentation', link: '/presentation' },
          { text: 'Installation', link: '/installation' },
          { text: 'Configuration', link: '/configuration' },
        ]
      },
	  {
        text: 'Difficulties',
        items: [
          { text: 'Global difficulty', link: '/difficulty/global' },
          { text: 'Player difficulty', link: '/difficulty/player' },
          { text: 'Passive difficulty', link: '/difficulty/passive' }
        ]
      },
	  {
        text: 'Other',
        items: [
          { text: 'Boss', link: '/other/boss' },
          { text: 'Commands', link: '/other/commands' },
          { text: 'Bounty', link: '/other/bounty' }
        ]
      }
    ],

    socialLinks: [
      {icon: 'github', link: 'https://github.com/architects-land/difficulty-death-scaler'},
      {icon: 'youtube', link: 'https://youtu.be/aPTg2FTSXP8'},
    ],

    footer: {
       message: 'Released under the AGPL License. <a href="https://www.anhgelus.world/legal/" target="_blank">Legal information</a>.',
       copyright: 'Copyright © 2025 Architects Land and William Hergès'
    }
  }
})

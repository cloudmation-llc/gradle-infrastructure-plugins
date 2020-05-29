module.exports = {
  title: 'Gradle Project Plugins by Cloudmation',
  url: 'https://your-docusaurus-test-site.com',
  baseUrl: '/',
  favicon: 'img/favicon.ico',
  organizationName: 'cloudmation-llc', // Usually your GitHub org/user name.
  projectName: 'cloudmation-gradle-project-plugins', // Usually your repo name.
  themeConfig: {
    disableDarkMode: true,
    navbar: {
      title: 'Gradle Project Plugins by Cloudmation',
      logo: {
        alt: 'Cloudmation LLC Logo',
        src: 'img/logo.svg',
      },
      links: [
        {
          href: 'https://github.com/cloudmation-llc/cloudmation-gradle-project-plugins',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Style Guide',
              to: 'doc1',
            },
            {
              label: 'Second Doc',
              to: 'doc2',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/cloudmation-llc/cloudmation-gradle-project-plugins',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Cloudmation LLC. Built with Docusaurus.`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          routeBasePath: '/',
          sidebarPath: require.resolve('./sidebars.js')/*,
          // Please change this to your repo.
          editUrl:
            'https://github.com/facebook/docusaurus/edit/master/website/',*/
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};

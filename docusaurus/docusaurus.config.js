module.exports = {
  title: 'Infrastructure Gradle Plugins by Cloudmation',
  url: 'https://your-docusaurus-test-site.com',
  baseUrl: '/',
  favicon: 'img/favicon.ico',
  organizationName: 'cloudmation-llc',
  projectName: 'gradle-infrastructure-plugins',
  themeConfig: {
    disableDarkMode: true,
    prism: {
      additionalLanguages: ['groovy', 'properties']  
    },
    navbar: {
      title: 'Infrastructure Gradle Plugins by Cloudmation',
      logo: {
        alt: 'Cloudmation LLC Logo',
        src: 'img/logo.svg',
      },
      links: [
        {
          href: 'https://github.com/cloudmation-llc/gradle-infrastructure-plugins',
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
              label: 'About',
              to: 'about',
            }
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/cloudmation-llc/gradle-infrastructure-plugins',
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
          customCss: require.resolve('./src/css/custom.css')
        }
      },
    ],
  ],
};

module.exports = {
  mainSidebar: [
    {
      type: 'doc',
      id: 'about'
    },
    {
      type: 'category',
      label: 'AWS (Amazon Web Services)',
      collapsed: false,
      items: [
        { type: 'doc', id: 'aws/global-config' },
        {
          type: 'category',
          collapsed: false,
          label: 'CloudFormation',
          items: [
            'aws/cloudformation/cf-getting-started',
            'aws/cloudformation/cf-tutorial-basics',
            'aws/cloudformation/cf-config',
            'aws/cloudformation/cf-cookbook'
          ]
        },
        'aws/roadmap'
      ]
    }
  ]
};

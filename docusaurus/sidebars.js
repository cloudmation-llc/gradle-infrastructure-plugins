module.exports = {
  mainSidebar: [
    {
      type: 'doc',
      id: 'about'
    },
    {
      type: 'category',
      label: 'AWS (Amazon Web Services)',
      items: [
        { type: 'doc', id: 'aws/global-config' },
        {
          type: 'category',
          label: 'CloudFormation',
          items: [
            'aws/cloudformation/cf-getting-started',
            'aws/cloudformation/cf-tutorial',
            'aws/cloudformation/cf-config'
          ]
        },
        'aws/roadmap'
      ]
    }
  ]
};

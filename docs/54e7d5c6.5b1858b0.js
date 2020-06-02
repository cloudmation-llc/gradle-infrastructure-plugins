(window.webpackJsonp=window.webpackJsonp||[]).push([[6],{106:function(e,t,a){"use strict";a.r(t),a.d(t,"frontMatter",(function(){return i})),a.d(t,"metadata",(function(){return l})),a.d(t,"rightToc",(function(){return c})),a.d(t,"default",(function(){return p}));var n=a(2),r=a(6),o=(a(0),a(114)),i={id:"cf-getting-started",title:"Getting Started",sidebar_label:"Getting Started"},l={id:"aws/cloudformation/cf-getting-started",title:"Getting Started",description:"There are a number of plugins for Gradle that provide various levels of integration with AWS. This family of plugins focuses on using CloudFormation as the principal tool to manage your AWS environment. The objective is not merely to lift AWS SDK calls to the surface as Gradle tasks, but to actually model an opinionated set of conventions that help you effectively organize and manage AWS resources.",source:"@site/docs/aws/cloudformation/getting-started.md",permalink:"/gradle-infrastructure-plugins/aws/cloudformation/cf-getting-started",sidebar_label:"Getting Started",sidebar:"mainSidebar",previous:{title:"Global Configuration",permalink:"/gradle-infrastructure-plugins/aws/global-config"},next:{title:"Tutorial",permalink:"/gradle-infrastructure-plugins/aws/cloudformation/cf-tutorial"}},c=[{value:"Features",id:"features",children:[]},{value:"Requirements",id:"requirements",children:[]},{value:"Installation",id:"installation",children:[{value:"Starter Template",id:"starter-template",children:[]},{value:"Manual Installation",id:"manual-installation",children:[]}]}],s={rightToc:c};function p(e){var t=e.components,a=Object(r.a)(e,["components"]);return Object(o.b)("wrapper",Object(n.a)({},s,a,{components:t,mdxType:"MDXLayout"}),Object(o.b)("p",null,"There are a number of plugins for Gradle that provide various levels of integration with AWS. This family of plugins focuses on using ",Object(o.b)("a",Object(n.a)({parentName:"p"},{href:"https://aws.amazon.com/cloudformation"}),"CloudFormation")," as the ",Object(o.b)("strong",{parentName:"p"},"principal")," tool to manage your AWS environment. The objective is not merely to lift AWS SDK calls to the surface as Gradle tasks, but to actually model an ",Object(o.b)("em",{parentName:"p"},"opinionated")," set of conventions that help you effectively organize and manage AWS resources."),Object(o.b)("p",null,'CloudFormation is an excellent service offering from AWS that provides ingredients and a domain specific language (using either YAML or JSON) to design most of your AWS environment in written form as templates. The templates in turn are used to create and update "stacks" of live resources.'),Object(o.b)("h2",{id:"features"},"Features"),Object(o.b)("ul",null,Object(o.b)("li",{parentName:"ul"},"Automatic generation of deployment tasks for each CloudFormation template."),Object(o.b)("li",{parentName:"ul"},"Incorporates ",Object(o.b)("a",Object(n.a)({parentName:"li"},{href:"https://github.com/aws-cloudformation/cfn-python-lint"}),"cfn-lint")," to ensure high template quality and best practices before deployment."),Object(o.b)("li",{parentName:"ul"},"Declarative configuration that starts with the root project, and can be selectively overidden at the subproject, or even ",Object(o.b)("em",{parentName:"li"},"per task"),".",Object(o.b)("ul",{parentName:"li"},Object(o.b)("li",{parentName:"ul"},"It is simple and straight forward to organize a complex environment across multiple regions, and with different IAM role requirements. You might think of this as a more powerful take on ",Object(o.b)("a",Object(n.a)({parentName:"li"},{href:"https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/what-is-cfnstacksets.html"}),"stack sets"),"."))),Object(o.b)("li",{parentName:"ul"},"Enhanced CLI experience including live, colored output of stack events during deployment."),Object(o.b)("li",{parentName:"ul"},"Uses ",Object(o.b)("a",Object(n.a)({parentName:"li"},{href:"https://github.com/aws/aws-sdk-java-v2"}),"AWS SDK for Java V2")," under the hood")),Object(o.b)("h2",{id:"requirements"},"Requirements"),Object(o.b)("ul",null,Object(o.b)("li",{parentName:"ul"},"At least Java 8 JDK or greater ",Object(o.b)("em",{parentName:"li"},"(this project is regularly tested with the ",Object(o.b)("a",Object(n.a)({parentName:"em"},{href:"https://aws.amazon.com/corretto/"}),"AWS Coretto")," JVM)")),Object(o.b)("li",{parentName:"ul"},Object(o.b)("a",Object(n.a)({parentName:"li"},{href:"https://github.com/aws-cloudformation/cfn-python-lint"}),"cfn-lint")," installed for template linting prior to deployment")),Object(o.b)("h2",{id:"installation"},"Installation"),Object(o.b)("p",null,"Regardless of which option you choose below, the steps below will cover getting a blank project created and set up. The ",Object(o.b)("a",Object(n.a)({parentName:"p"},{href:"cf-tutorial"}),"tutorial")," walks through a basic AWS configuration in greater detail including working with templates, and the auto-generated tasks."),Object(o.b)("h3",{id:"starter-template"},"Starter Template"),Object(o.b)("p",null,"One option to get going quickly to clone the AWS starter template into a new project. The template project includes a preconfigured Gradle wrapper so that you do not need to install Gradle yourself."),Object(o.b)("p",null,"A helper script is provided that will clone the template repo, and then replace the Git configuration with a new empty repo ready for your work."),Object(o.b)("p",null,"Copy and paste the line below to download and run the ",Object(o.b)("inlineCode",{parentName:"p"},"create-aws-project.sh")," script straight from GitHub. Replace ",Object(o.b)("inlineCode",{parentName:"p"},"YOUR_DIRECTORY")," with a destination name of your choosing."),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-bash"}),' curl -L "https://raw.githubusercontent.com/cloudmation-llc/gradle-infrastructure-plugins/master/create-aws-project.sh" | bash -s YOUR_DIRECTORY\n')),Object(o.b)("h3",{id:"manual-installation"},"Manual Installation"),Object(o.b)("p",null,"Alternatively, you can set up a new project yourself by going through a few manual steps. You will need a recent version of Gradle already installed on your workstation. Verify by first running ",Object(o.b)("inlineCode",{parentName:"p"},"gradle --version"),"."),Object(o.b)("ol",null,Object(o.b)("li",{parentName:"ol"},"Create a new working directory for your project."),Object(o.b)("li",{parentName:"ol"},"In your directory, create three empty files:")),Object(o.b)("ul",null,Object(o.b)("li",{parentName:"ul"},Object(o.b)("inlineCode",{parentName:"li"},"gradle.properties")),Object(o.b)("li",{parentName:"ul"},Object(o.b)("inlineCode",{parentName:"li"},"settings.gradle")),Object(o.b)("li",{parentName:"ul"},Object(o.b)("inlineCode",{parentName:"li"},"build.gradle"))),Object(o.b)("ol",{start:3},Object(o.b)("li",{parentName:"ol"},"It is recommended to externalize the plugin version as a Gradle property. Copy and paste the following into ",Object(o.b)("inlineCode",{parentName:"li"},"gradle.properties"),".")),Object(o.b)("p",null,"Check the Gradle plugins portal for the latest version string: ",Object(o.b)("a",Object(n.a)({parentName:"p"},{href:"https://plugins.gradle.org/plugin/com.cloudmation.aws"}),"https://plugins.gradle.org/plugin/com.cloudmation.aws")),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-properties"}),"cloudmationInfraPluginsVersion = VERSION_HERE\n")),Object(o.b)("ol",null,Object(o.b)("li",{parentName:"ol"},"Add the AWS project settings plugin to ",Object(o.b)("inlineCode",{parentName:"li"},"settings.gradle"),".")),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-groovy"}),'plugins {\n    id "com.cloudmation.aws-settings-cloudformation" version "$cloudmationInfraPluginsVersion"\n}\n')),Object(o.b)("ol",{start:5},Object(o.b)("li",{parentName:"ol"},"Add the AWS project config and CloudFormation plugins to ",Object(o.b)("inlineCode",{parentName:"li"},"build.gradle"),".")),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-groovy"}),'plugins {\n    id "com.cloudmation.aws"\n    id "com.cloudmation.aws-cloudformation"\n}\n')),Object(o.b)("ol",{start:6},Object(o.b)("li",{parentName:"ol"},Object(o.b)("p",{parentName:"li"},"Test that the configuration works by running ",Object(o.b)("inlineCode",{parentName:"p"},"gradle tasks"),". Gradle should list the available tasks and no errors or failures.")),Object(o.b)("li",{parentName:"ol"},Object(o.b)("p",{parentName:"li"},Object(o.b)("em",{parentName:"p"},"Optionally"),", create a Gradle wrapper by running ",Object(o.b)("inlineCode",{parentName:"p"},"gradle wrapper"),". This is highly recommended especially if you plan to develop a CI/CD pipeline for pushing infrastructure changes, or when multiple individuals will be working from this repo.")),Object(o.b)("li",{parentName:"ol"},Object(o.b)("p",{parentName:"li"},"If this is your first time using the plugins, check out the ",Object(o.b)("a",Object(n.a)({parentName:"p"},{href:"cf-tutorial"}),"tutorial")," next."))))}p.isMDXComponent=!0},114:function(e,t,a){"use strict";a.d(t,"a",(function(){return u})),a.d(t,"b",(function(){return m}));var n=a(0),r=a.n(n);function o(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function i(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,n)}return a}function l(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?i(Object(a),!0).forEach((function(t){o(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):i(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}function c(e,t){if(null==e)return{};var a,n,r=function(e,t){if(null==e)return{};var a,n,r={},o=Object.keys(e);for(n=0;n<o.length;n++)a=o[n],t.indexOf(a)>=0||(r[a]=e[a]);return r}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(n=0;n<o.length;n++)a=o[n],t.indexOf(a)>=0||Object.prototype.propertyIsEnumerable.call(e,a)&&(r[a]=e[a])}return r}var s=r.a.createContext({}),p=function(e){var t=r.a.useContext(s),a=t;return e&&(a="function"==typeof e?e(t):l(l({},t),e)),a},u=function(e){var t=p(e.components);return r.a.createElement(s.Provider,{value:t},e.children)},b={inlineCode:"code",wrapper:function(e){var t=e.children;return r.a.createElement(r.a.Fragment,{},t)}},d=r.a.forwardRef((function(e,t){var a=e.components,n=e.mdxType,o=e.originalType,i=e.parentName,s=c(e,["components","mdxType","originalType","parentName"]),u=p(a),d=n,m=u["".concat(i,".").concat(d)]||u[d]||b[d]||o;return a?r.a.createElement(m,l(l({ref:t},s),{},{components:a})):r.a.createElement(m,l({ref:t},s))}));function m(e,t){var a=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var o=a.length,i=new Array(o);i[0]=d;var l={};for(var c in t)hasOwnProperty.call(t,c)&&(l[c]=t[c]);l.originalType=e,l.mdxType="string"==typeof e?e:n,i[1]=l;for(var s=2;s<o;s++)i[s]=a[s];return r.a.createElement.apply(null,i)}return r.a.createElement.apply(null,a)}d.displayName="MDXCreateElement"}}]);
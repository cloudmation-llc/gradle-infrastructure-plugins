(window.webpackJsonp=window.webpackJsonp||[]).push([[4],{104:function(e,t,a){"use strict";a.r(t),a.d(t,"frontMatter",(function(){return i})),a.d(t,"metadata",(function(){return c})),a.d(t,"rightToc",(function(){return l})),a.d(t,"default",(function(){return b}));var n=a(2),r=a(6),o=(a(0),a(114)),i=(a(115),{id:"cf-config",title:"CloudFormation Configuration",sidebar_label:"Configuration"}),c={id:"aws/cloudformation/cf-config",title:"CloudFormation Configuration",description:"Building on the services provided by the global configuration, the CloudFormation plugin can be further customized by adding a cloudformation block to an aws block as demonstrated below:",source:"@site/docs/aws/cloudformation/configuration.md",permalink:"/gradle-infrastructure-plugins/aws/cloudformation/cf-config",sidebar_label:"Configuration",sidebar:"mainSidebar",previous:{title:"Tutorial",permalink:"/gradle-infrastructure-plugins/aws/cloudformation/cf-tutorial"},next:{title:"Roadmap",permalink:"/gradle-infrastructure-plugins/aws/roadmap"}},l=[{value:"Customize Stack Naming",id:"customize-stack-naming",children:[]},{value:"Customize Task Generation",id:"customize-task-generation",children:[{value:"Task Naming",id:"task-naming",children:[]},{value:"Task Grouping",id:"task-grouping",children:[]},{value:"Including and Excluding Tasks",id:"including-and-excluding-tasks",children:[]}]},{value:"Custom Stack Definition",id:"custom-stack-definition",children:[]}],s={rightToc:l};function b(e){var t=e.components,a=Object(r.a)(e,["components"]);return Object(o.b)("wrapper",Object(n.a)({},s,a,{components:t,mdxType:"MDXLayout"}),Object(o.b)("p",null,"Building on the services provided by the ",Object(o.b)("a",Object(n.a)({parentName:"p"},{href:"../global-config"}),"global configuration"),", the CloudFormation plugin can be further customized by adding a ",Object(o.b)("inlineCode",{parentName:"p"},"cloudformation")," block to an ",Object(o.b)("inlineCode",{parentName:"p"},"aws")," block as demonstrated below:"),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-groovy"}),'aws {\n    cloudformation {\n        capabilities = [""] // Set capabilities to be used for the stack deployment\n        failOnEmptyChangeset = false\n        parameterOverrides = [:] // A key-value map of parameter overrides provided to the stack at deployment\n        roleArn = "" // IAM role that CloudFormation will assume for deployment\n    }\n}\n')),Object(o.b)("table",null,Object(o.b)("thead",{parentName:"table"},Object(o.b)("tr",{parentName:"thead"},Object(o.b)("th",Object(n.a)({parentName:"tr"},{align:null}),"Property"),Object(o.b)("th",Object(n.a)({parentName:"tr"},{align:null}),"Default Value"),Object(o.b)("th",Object(n.a)({parentName:"tr"},{align:null}),"Description"))),Object(o.b)("tbody",{parentName:"table"},Object(o.b)("tr",{parentName:"tbody"},Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),Object(o.b)("inlineCode",{parentName:"td"},"capabilities")),Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),"None"),Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),"For certain cases, you will need to set IAM capabilities. See the ",Object(o.b)("a",Object(n.a)({parentName:"td"},{href:"https://docs.aws.amazon.com/AWSCloudFormation/latest/APIReference/API_CreateStack.html"}),"create stack API reference"),".")),Object(o.b)("tr",{parentName:"tbody"},Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),Object(o.b)("inlineCode",{parentName:"td"},"failOnEmptyChangeset")),Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),Object(o.b)("inlineCode",{parentName:"td"},"false")),Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),"If ",Object(o.b)("inlineCode",{parentName:"td"},"true"),", a changeset that is created for a stack that results in no resource changes will cause the build to fail. ",Object(o.b)("inlineCode",{parentName:"td"},"false")," ignores this situation, and allows the build to succeed.")),Object(o.b)("tr",{parentName:"tbody"},Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),Object(o.b)("inlineCode",{parentName:"td"},"parameterOverrides")),Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),"None"),Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),"Map of key-value parameters that override any default parameters specified in the template. ",Object(o.b)("em",{parentName:"td"},"The same merging rules used for resource tags (see above) also apply to parameter overrides."))),Object(o.b)("tr",{parentName:"tbody"},Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),Object(o.b)("inlineCode",{parentName:"td"},"roleArn")),Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),"None"),Object(o.b)("td",Object(n.a)({parentName:"tr"},{align:null}),"Set an IAM role that CloudFormation will assume for the stack deployment. ",Object(o.b)("a",Object(n.a)({parentName:"td"},{href:"https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-iam-servicerole.html"}),"See AWS documentation"),".")))),Object(o.b)("h2",{id:"customize-stack-naming"},"Customize Stack Naming"),Object(o.b)("p",null,"By default, a stack deployment task uses the filename of its template, and the name of its project to derive the stack name. For example, a template named ",Object(o.b)("inlineCode",{parentName:"p"},"vpc.yml")," in a subproject named ",Object(o.b)("inlineCode",{parentName:"p"},"network")," will have the stack name ",Object(o.b)("inlineCode",{parentName:"p"},"network-vpc"),"."),Object(o.b)("p",null,"There are ways to override this behavior:"),Object(o.b)("p",null,Object(o.b)("strong",{parentName:"p"},"Override Stack Prefix")),Object(o.b)("p",null,"Instead of using the project name to prefix the stack name, you can specify a custom prefix that all of the deploy tasks in the project will use."),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-groovy"}),'aws {\n    cloudformation {\n        stackPrefix = "super-awesome-network"\n    }\n}\n')),Object(o.b)("p",null,"Using the example above, if you have templates named ",Object(o.b)("inlineCode",{parentName:"p"},"vpc.yml"),", and ",Object(o.b)("inlineCode",{parentName:"p"},"vpc-zone-a.yml"),", then the respective stack names at deployment will be ",Object(o.b)("inlineCode",{parentName:"p"},"super-awesome-network-vpc")," and ",Object(o.b)("inlineCode",{parentName:"p"},"super-awesome-network-vpc-zone-a"),"."),Object(o.b)("p",null,Object(o.b)("strong",{parentName:"p"},"Override Stack Name Per Task")),Object(o.b)("p",null,"The configuration for automatically generated tasks can be changed even after they are created. For example, if you have a subproject named ",Object(o.b)("inlineCode",{parentName:"p"},"network"),", then there will be a ",Object(o.b)("inlineCode",{parentName:"p"},"network.gradle")," in the project directory. Open that file, and add an ",Object(o.b)("inlineCode",{parentName:"p"},"afterEvaluate")," block. You can use the task reconfiguration DSL to change the properties for any generated task."),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-groovy"}),'afterEvaluate {\n    deployVpc.configure {\n        aws {\n            cloudformation {\n                stackName = "super-awesome-vpc"\n            }\n        }\n    }\n}\n')),Object(o.b)("h2",{id:"customize-task-generation"},"Customize Task Generation"),Object(o.b)("p",null,"If you want to customize the tasks that are automatically generated by the plugin, add a ",Object(o.b)("inlineCode",{parentName:"p"},"taskGeneration")," config block to the ",Object(o.b)("inlineCode",{parentName:"p"},"cloudformation")," block. ",Object(o.b)("strong",{parentName:"p"},"This is only supported at the subproject level.")),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-groovy"}),'aws {\n    cloudformation {\n        taskGeneration {\n            group = "Some other group"    \n        }\n    }\n}\n')),Object(o.b)("h3",{id:"task-naming"},"Task Naming"),Object(o.b)("p",null,"The plugin follow the Gradle convention of using camel casing for task names. The characters ",Object(o.b)("inlineCode",{parentName:"p"},"-")," ",Object(o.b)("inlineCode",{parentName:"p"},".")," ",Object(o.b)("inlineCode",{parentName:"p"},"_")," and ",Object(o.b)("inlineCode",{parentName:"p"}," ")," (space) are declared as delimiters which are fed into the camel case routine. Dashes (",Object(o.b)("inlineCode",{parentName:"p"},"-"),") are very readable and encouraged for naming files and custom stacks."),Object(o.b)("h3",{id:"task-grouping"},"Task Grouping"),Object(o.b)("p",null,"Gradle tasks support a ",Object(o.b)("inlineCode",{parentName:"p"},"group")," property to logically group tasks together under categories. Using grouping provides for a neater appearance in tools that display lists of tasks such as when you run ",Object(o.b)("inlineCode",{parentName:"p"},"gradle tasks")," on the command line. This is recommended."),Object(o.b)("p",null,"By default, the group will be set to ",Object(o.b)("inlineCode",{parentName:"p"},"aws"),", but you can change this to anything. For example you might consider grouping IAM tasks and network tasks separately."),Object(o.b)("h3",{id:"including-and-excluding-tasks"},"Including and Excluding Tasks"),Object(o.b)("p",null,"There could be use cases when you want to alter which tasks are generated by the plugin. For example, you want to use use the custom stacks feature ",Object(o.b)("em",{parentName:"p"},"(see below)")," with a specific template file, and not have tasks generated automatically for that template. You can create rules that either use a ",Object(o.b)("a",Object(n.a)({parentName:"p"},{href:"https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html"}),"regular expression match")," on the task name, or you can provide a closure to use your own logic."),Object(o.b)("p",null,"Include rules are evaluated first, and then exclude rules are evaluated after. You can call ",Object(o.b)("inlineCode",{parentName:"p"},"include")," or ",Object(o.b)("inlineCode",{parentName:"p"},"exclude")," as many times as you want. Rules are evaluated in creation order."),Object(o.b)("h4",{id:"exclude-by-pattern"},"Exclude by Pattern"),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-groovy"}),'aws {\n    cloudformation {\n        taskGeneration {\n            exclude ".*VpcSubnet.*"   \n        }\n    }\n}\n')),Object(o.b)("h4",{id:"exclude-by-closure"},"Exclude by Closure"),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-groovy"}),'aws {\n    cloudformation {\n        taskGeneration {\n            exclude { taskName -> taskName == "someTaskYouWantToAvoid" }\n        }\n    }\n}\n')),Object(o.b)("h4",{id:"include-by-pattern"},"Include by Pattern"),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-groovy"}),'aws {\n    cloudformation {\n        taskGeneration {\n            include "lint.*"   \n        }\n    }\n}\n')),Object(o.b)("h4",{id:"include-by-closure"},"Include by Closure"),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-groovy"}),'aws {\n    cloudformation {\n        taskGeneration {\n            include { taskName -> taskName == "someTaskYouWantToEnsureIsCreated" }\n        }\n    }\n}\n')),Object(o.b)("h2",{id:"custom-stack-definition"},"Custom Stack Definition"),Object(o.b)("p",null,"The idea of using convention-over-configuration can be very convenient, but the default behavior may also generate tasks that you do not want, and you would like finer control over how a template is going to be deployed. Custom stacks skip the convention, and let you define the specific stacks to be deployed."),Object(o.b)("p",null,"Add a ",Object(o.b)("inlineCode",{parentName:"p"},"stacks")," block to the ",Object(o.b)("inlineCode",{parentName:"p"},"cloudformation")," block. Below is a more complex example that sets a custom task group name, and uses a single template with parameterization to generate deployment tasks."),Object(o.b)("p",null,"Note that name in quotes which defines each custom stack is used as part of the task name generation. See the ",Object(o.b)("em",{parentName:"p"},"Task Naming")," section above for more detail on naming."),Object(o.b)("pre",null,Object(o.b)("code",Object(n.a)({parentName:"pre"},{className:"language-groovy"}),'aws {\n    cloudformation {\n        taskGeneration {\n            group = "AWS Network"\n        }\n\n        stacks {\n            "subnet-a" {\n                stackName = "vpc-subnet-a"\n                template = file("vpc-subnet.yml")\n                parameterOverrides = [\n                    "PrivateRange": "10.255.1.0/24",\n                    "PublicRange": "10.255.251.0/24",\n                    "RegionAzIndex": "0"]\n            }\n\n            "subnet-b" {\n                stackName = "vpc-subnet-b"\n                template = file("vpc-subnet.yml")\n                parameterOverrides = [\n                    "PrivateRange": "10.255.2.0/24",\n                    "PublicRange": "10.255.252.0/24",\n                    "RegionAzIndex": "1"]\n            }\n\n            "subnet-c" {\n                stackName = "vpc-subnet-c"\n                template = file("vpc-subnet.yml")\n                parameterOverrides = [\n                    "PrivateRange": "10.255.3.0/24",\n                    "PublicRange": "10.255.253.0/24"\n                    "RegionAzIndex": "2"]\n            }\n\n            "subnet-d" {\n                stackName = "vpc-subnet-d"\n                template = file("vpc-subnet.yml")\n                parameterOverrides = [\n                    "PrivateRange": "10.255.4.0/24",\n                    "PublicRange": "10.255.254.0/24",\n                    "RegionAzIndex": "3"]\n            }\n        }\n    }\n}\n')),Object(o.b)("p",null,"Running ",Object(o.b)("inlineCode",{parentName:"p"},"gradle tasks")," will display the tasks that are generated as a result."),Object(o.b)("p",null,Object(o.b)("img",Object(n.a)({parentName:"p"},{src:"/img/screenshots/aws-cf-config-custom-tasks.png",alt:"Example of custom stacks"}))))}b.isMDXComponent=!0},113:function(e,t,a){"use strict";var n=a(0),r=a(33);t.a=function(){return Object(n.useContext)(r.a)}},114:function(e,t,a){"use strict";a.d(t,"a",(function(){return u})),a.d(t,"b",(function(){return m}));var n=a(0),r=a.n(n);function o(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function i(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,n)}return a}function c(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?i(Object(a),!0).forEach((function(t){o(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):i(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}function l(e,t){if(null==e)return{};var a,n,r=function(e,t){if(null==e)return{};var a,n,r={},o=Object.keys(e);for(n=0;n<o.length;n++)a=o[n],t.indexOf(a)>=0||(r[a]=e[a]);return r}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(n=0;n<o.length;n++)a=o[n],t.indexOf(a)>=0||Object.prototype.propertyIsEnumerable.call(e,a)&&(r[a]=e[a])}return r}var s=r.a.createContext({}),b=function(e){var t=r.a.useContext(s),a=t;return e&&(a="function"==typeof e?e(t):c(c({},t),e)),a},u=function(e){var t=b(e.components);return r.a.createElement(s.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return r.a.createElement(r.a.Fragment,{},t)}},d=r.a.forwardRef((function(e,t){var a=e.components,n=e.mdxType,o=e.originalType,i=e.parentName,s=l(e,["components","mdxType","originalType","parentName"]),u=b(a),d=n,m=u["".concat(i,".").concat(d)]||u[d]||p[d]||o;return a?r.a.createElement(m,c(c({ref:t},s),{},{components:a})):r.a.createElement(m,c({ref:t},s))}));function m(e,t){var a=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var o=a.length,i=new Array(o);i[0]=d;var c={};for(var l in t)hasOwnProperty.call(t,l)&&(c[l]=t[l]);c.originalType=e,c.mdxType="string"==typeof e?e:n,i[1]=c;for(var s=2;s<o;s++)i[s]=a[s];return r.a.createElement.apply(null,i)}return r.a.createElement.apply(null,a)}d.displayName="MDXCreateElement"},115:function(e,t,a){"use strict";a.d(t,"a",(function(){return r}));var n=a(113);function r(e){const{siteConfig:t}=Object(n.a)(),{baseUrl:a="/"}=t||{};if(!e)return e;return/^(https?:|\/\/)/.test(e)?e:e.startsWith("/")?a+e.slice(1):a+e}}}]);
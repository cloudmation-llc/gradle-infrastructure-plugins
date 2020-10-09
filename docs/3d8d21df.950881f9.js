(window.webpackJsonp=window.webpackJsonp||[]).push([[6],{106:function(e,t,r){"use strict";r.r(t),r.d(t,"frontMatter",(function(){return i})),r.d(t,"metadata",(function(){return c})),r.d(t,"rightToc",(function(){return s})),r.d(t,"default",(function(){return u}));var n=r(2),o=r(6),a=(r(0),r(116)),i={id:"about",title:"About",sidebar_label:"About"},c={id:"about",isDocsHomePage:!1,title:"About",description:"What is this?",source:"@site/docs/about.md",permalink:"/gradle-infrastructure-plugins/docs/about",sidebar_label:"About",sidebar:"mainSidebar",next:{title:"Global Configuration",permalink:"/gradle-infrastructure-plugins/docs/aws/global-config"}},s=[{value:"What is this?",id:"what-is-this",children:[]},{value:"Why Infrastructure as Code?",id:"why-infrastructure-as-code",children:[{value:"Gradle Crash Course",id:"gradle-crash-course",children:[]}]}],l={rightToc:s};function u(e){var t=e.components,r=Object(o.a)(e,["components"]);return Object(a.b)("wrapper",Object(n.a)({},l,r,{components:t,mdxType:"MDXLayout"}),Object(a.b)("h2",{id:"what-is-this"},"What is this?"),Object(a.b)("p",null,"A set of plugins for ",Object(a.b)("a",Object(n.a)({parentName:"p"},{href:"https://gradle.org/"}),"Gradle")," that provide an opinionated set of conventions for setting up and managing infrastructure-as-code projects."),Object(a.b)("p",null,"Deploying and managing cloud infrastructure using configuration-as-code in this author's view is vital for long term success. When you consider that cloud providers provide official tooling for their services, add into the mix community projects for linting, static analysis, image building, host automation, and more . . . the amount tools that you end up using manage your cloud environment can be significant."),Object(a.b)("p",null,'Those who develop software have long understood this problem, and the community has built excellent tools to solve it. This is where Gradle comes in. Gradle is not just well suited organizing software development projects, it can also be great for "developing" your infrastructure, too.'),Object(a.b)("h2",{id:"why-infrastructure-as-code"},"Why Infrastructure as Code?"),Object(a.b)("p",null,"For many organizations and projects, excellent infrastructure is key. Some businesses would cease to exist without a well run and maintained cloud environment."),Object(a.b)("p",null,Object(a.b)("em",{parentName:"p"},"Infrastructure-as-code")," brings some of the time honored practices of software development such change control and rapid continuous integration into the delivery of compute services. It allows you describe your operating environment is in a way that is documented, teachible, reproducible, and auditable."),Object(a.b)("p",null,"In contrast, were you to interact with your cloud provider exclusively through a web GUI, an enormous amount of operational knowledge is lost in the mouse clicks. Sure, you could reconstruct some of it through an effective audit strategy that captures console and API actions. However, whatever you may cobble together will never be as complete as having the environment completely down in writing, with comments, and a change history. Morever, having a written copy of your environment is the first step to using automation effectively for long-term maintenance."),Object(a.b)("h3",{id:"gradle-crash-course"},"Gradle Crash Course"),Object(a.b)("p",null,"Paraphrasing from the page ",Object(a.b)("a",Object(n.a)({parentName:"p"},{href:"https://docs.gradle.org/current/userguide/what_is_gradle.html#what_is_gradle"}),"What is Gradle"),", projects are made easy to build by implementing conventions. You can show up and not need to think deeply about how your infrastructure templates/code/pipelines/plans/playbooks should be organized. Gradle can automate that for you. The beauty of Gradle is that while there are recommended conventions that work well out of the box, it is very easy to override and customize to suit specific needs."),Object(a.b)("p",null,"See also ",Object(a.b)("a",Object(n.a)({parentName:"p"},{href:"https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#sec:use_standard_conventions"}),"Convention over Configuration ",Object(a.b)("em",{parentName:"a"},"(Organizing Gradle Projects)"))),Object(a.b)("h4",{id:"the-root-project"},'The "Root" Project'),Object(a.b)("p",null,"Any directory with a ",Object(a.b)("inlineCode",{parentName:"p"},"build.gradle")," file becomes the root of a project. Subprojects can be defined in subdirectories with additional ",Object(a.b)("inlineCode",{parentName:"p"},".gradle")," files, thus forming a hierarchy."),Object(a.b)("h4",{id:"subprojects"},"Subprojects"),Object(a.b)("p",null,"Gradle subprojects can have their own configuration independent of other projects, but can also derive and override configuration from the root project, too."),Object(a.b)("p",null,"If we were to visualize this using a simple nested list, it might look like the following:"),Object(a.b)("ul",null,Object(a.b)("li",{parentName:"ul"},"Root",Object(a.b)("ul",{parentName:"li"},Object(a.b)("li",{parentName:"ul"},"Project A",Object(a.b)("ul",{parentName:"li"},Object(a.b)("li",{parentName:"ul"},"Project AA"),Object(a.b)("li",{parentName:"ul"},"Project AB"),Object(a.b)("li",{parentName:"ul"},"Project AC"))),Object(a.b)("li",{parentName:"ul"},"Project B",Object(a.b)("ul",{parentName:"li"},Object(a.b)("li",{parentName:"ul"},"Project BA"))),Object(a.b)("li",{parentName:"ul"},"Project C")))),Object(a.b)("p",null,"Effective use of subprojects are one of the highlights when you use this plugin family for your cloud projects."),Object(a.b)("h4",{id:"tasks"},"Tasks"),Object(a.b)("p",null,"Gradle projects define tasks that go and perform actual work. Tasks can be created at the root for the entire project including children subprojects, or a subproject may define its own tasks for specific needs."))}u.isMDXComponent=!0},116:function(e,t,r){"use strict";r.d(t,"a",(function(){return d})),r.d(t,"b",(function(){return h}));var n=r(0),o=r.n(n);function a(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function i(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function c(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?i(Object(r),!0).forEach((function(t){a(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):i(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function s(e,t){if(null==e)return{};var r,n,o=function(e,t){if(null==e)return{};var r,n,o={},a=Object.keys(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||(o[r]=e[r]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(o[r]=e[r])}return o}var l=o.a.createContext({}),u=function(e){var t=o.a.useContext(l),r=t;return e&&(r="function"==typeof e?e(t):c(c({},t),e)),r},d=function(e){var t=u(e.components);return o.a.createElement(l.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return o.a.createElement(o.a.Fragment,{},t)}},b=o.a.forwardRef((function(e,t){var r=e.components,n=e.mdxType,a=e.originalType,i=e.parentName,l=s(e,["components","mdxType","originalType","parentName"]),d=u(r),b=n,h=d["".concat(i,".").concat(b)]||d[b]||p[b]||a;return r?o.a.createElement(h,c(c({ref:t},l),{},{components:r})):o.a.createElement(h,c({ref:t},l))}));function h(e,t){var r=arguments,n=t&&t.mdxType;if("string"==typeof e||n){var a=r.length,i=new Array(a);i[0]=b;var c={};for(var s in t)hasOwnProperty.call(t,s)&&(c[s]=t[s]);c.originalType=e,c.mdxType="string"==typeof e?e:n,i[1]=c;for(var l=2;l<a;l++)i[l]=r[l];return o.a.createElement.apply(null,i)}return o.a.createElement.apply(null,r)}b.displayName="MDXCreateElement"}}]);
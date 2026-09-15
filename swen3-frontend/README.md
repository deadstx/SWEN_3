
## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```


## Project coding styles:


- .css Imports ALWAYS and ONLY happen in the component itself
    e.g "UploadForm.tsx" imports "UploadForm.css"


- Components are written in CamelCase (First letter in uppercase)
    e.g "UploadForm.tsx"; "Sidebar.tsx"


- "styling" folder MUST be mirrored to "components" folder at all times
    e.g when writing a component ALWAYS copy it to styling as .css with the SAME NAME


- Always write dynamic .css styling (Colors, Text-Sizes, ...). Design in globals.css
    e.g background-color: var(--background)


- Document code changes and use git as often as possible  
    if some breaking chamges are done you can easily go back to working versions 


- when finished coding ALWAYS commit and push to GitHub  
    otherwise others can't get your code 

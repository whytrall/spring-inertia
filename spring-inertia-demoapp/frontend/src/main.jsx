import { createInertiaApp } from '@inertiajs/react'
import { createRoot } from 'react-dom/client'
import './styles.css'

// Import all page components
const pages = import.meta.glob('./Pages/**/*.jsx', { eager: true })

createInertiaApp({
  resolve: name => {
    const page = pages[`./Pages/${name}.jsx`]
    if (!page) {
      throw new Error(`Page not found: ${name}`)
    }
    return page
  },
  setup({ el, App, props }) {
    createRoot(el).render(<App {...props} />)
  },
})

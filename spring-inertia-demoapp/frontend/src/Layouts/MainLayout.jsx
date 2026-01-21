import { Link, usePage } from '@inertiajs/react'

export default function MainLayout({ children }) {
  const { navigation, currentPath, app, flash } = usePage().props

  return (
    <div className="app">
      <nav className="nav">
        <Link href="/" className="nav-brand">{app?.name || 'Inertia Demo'}</Link>
        <ul className="nav-links">
          {navigation?.map((item) => (
            <li key={item.href}>
              <Link
                href={item.href}
                className={currentPath === item.href ? 'active' : ''}
              >
                {item.name}
              </Link>
            </li>
          ))}
        </ul>
      </nav>

      <main className="main">
        {flash?.success && (
          <div className="alert alert-success">{flash.success}</div>
        )}
        {flash?.error && (
          <div className="alert alert-error">{flash.error}</div>
        )}
        {flash?.info && (
          <div className="alert alert-info">{flash.info}</div>
        )}
        {flash?.warning && (
          <div className="alert alert-warning">{flash.warning}</div>
        )}
        {children}
      </main>
    </div>
  )
}

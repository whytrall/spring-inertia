import { router } from '@inertiajs/react'
import { useState } from 'react'
import MainLayout from '../../Layouts/MainLayout'

export default function PropsIndex({
  timestamp,
  serverTime,
  lazyData,
  deferredStats,
  deferredAnalytics,
  config,
  notifications
}) {
  const [loading, setLoading] = useState({})

  const loadProp = (propName) => {
    setLoading(prev => ({ ...prev, [propName]: true }))
    router.reload({
      only: [propName],
      onFinish: () => setLoading(prev => ({ ...prev, [propName]: false }))
    })
  }

  const reloadAll = () => {
    router.reload()
  }

  return (
    <MainLayout>
      <div className="page-header">
        <h1>Props Demo</h1>
        <p>Demonstrating different prop types: lazy, deferred, always, once, and merge</p>
        <button className="btn btn-primary" onClick={reloadAll} style={{ marginTop: '1rem' }}>
          Full Reload
        </button>
      </div>

      <div className="grid grid-2">
        {/* Regular Prop */}
        <div className="card">
          <div className="card-header">Regular Prop</div>
          <p className="prop-label">timestamp</p>
          <div className="prop-value">{timestamp}</div>
          <p style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: '#666' }}>
            Always included, resolved on every request
          </p>
        </div>

        {/* Always Prop */}
        <div className="card">
          <div className="card-header">Always Prop</div>
          <p className="prop-label">serverTime</p>
          <div className="prop-value">{serverTime}</div>
          <p style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: '#666' }}>
            Always included even in partial reloads
          </p>
        </div>

        {/* Lazy Prop */}
        <div className="card">
          <div className="card-header">
            Lazy Prop
            <button
              className="btn btn-info"
              onClick={() => loadProp('lazyData')}
              disabled={loading.lazyData}
              style={{ marginLeft: '1rem', fontSize: '0.75rem' }}
            >
              {loading.lazyData ? 'Loading...' : 'Load Lazy Data'}
            </button>
          </div>
          <p className="prop-label">lazyData</p>
          {lazyData ? (
            <div className="prop-value">
              <pre>{JSON.stringify(lazyData, null, 2)}</pre>
            </div>
          ) : (
            <div className="prop-value" style={{ color: '#999' }}>
              Not loaded yet (excluded from initial load)
            </div>
          )}
          <p style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: '#666' }}>
            Must be explicitly requested via partial reload
          </p>
        </div>

        {/* Deferred Prop (default group) */}
        <div className="card">
          <div className="card-header">Deferred Prop (auto-loaded)</div>
          <p className="prop-label">deferredStats</p>
          {deferredStats ? (
            <div className="prop-value">
              <pre>{JSON.stringify(deferredStats, null, 2)}</pre>
            </div>
          ) : (
            <div className="prop-value">
              <span className="spinner"></span> Loading automatically...
            </div>
          )}
          <p style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: '#666' }}>
            Automatically fetched by frontend after page loads
          </p>
        </div>

        {/* Deferred Prop (analytics group) */}
        <div className="card">
          <div className="card-header">Deferred Prop (analytics group)</div>
          <p className="prop-label">deferredAnalytics</p>
          {deferredAnalytics ? (
            <div className="prop-value">
              <pre>{JSON.stringify(deferredAnalytics, null, 2)}</pre>
            </div>
          ) : (
            <div className="prop-value">
              <span className="spinner"></span> Loading automatically...
            </div>
          )}
          <p style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: '#666' }}>
            Deferred props can be grouped for batch loading
          </p>
        </div>

        {/* Once Prop */}
        <div className="card">
          <div className="card-header">Once Prop (cached)</div>
          <p className="prop-label">config</p>
          {config ? (
            <div className="prop-value">
              <pre>{JSON.stringify(config, null, 2)}</pre>
            </div>
          ) : (
            <div className="prop-value" style={{ color: '#999' }}>
              Loading...
            </div>
          )}
          <p style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: '#666' }}>
            Resolved once and cached on client with TTL
          </p>
        </div>

        {/* Merge Prop */}
        <div className="card">
          <div className="card-header">
            Merge Prop
            <button
              className="btn btn-warning"
              onClick={() => loadProp('notifications')}
              disabled={loading.notifications}
              style={{ marginLeft: '1rem', fontSize: '0.75rem' }}
            >
              {loading.notifications ? 'Loading...' : 'Add Notification'}
            </button>
          </div>
          <p className="prop-label">notifications</p>
          <div className="prop-value">
            {notifications?.length > 0 ? (
              <ul style={{ margin: 0, paddingLeft: '1.25rem' }}>
                {notifications.map((n, i) => (
                  <li key={i}>{n.message}</li>
                ))}
              </ul>
            ) : (
              'No notifications'
            )}
          </div>
          <p style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: '#666' }}>
            New data is merged with existing client data
          </p>
        </div>
      </div>
    </MainLayout>
  )
}

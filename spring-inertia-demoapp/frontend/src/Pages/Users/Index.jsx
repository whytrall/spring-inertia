import { Link, router } from '@inertiajs/react'
import { useState } from 'react'
import MainLayout from '../../Layouts/MainLayout'

export default function UsersIndex({ users, pagination, stats }) {
  const [loading, setLoading] = useState(false)

  const loadStats = () => {
    setLoading(true)
    router.reload({
      only: ['stats'],
      onFinish: () => setLoading(false)
    })
  }

  return (
    <MainLayout>
      <div className="page-header">
        <h1>Users</h1>
        <p>Demonstrating pagination and lazy/deferred props</p>
      </div>

      <div className="grid grid-2">
        <div className="card">
          <div className="card-header">User List</div>
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
              </tr>
            </thead>
            <tbody>
              {users?.map((user) => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>{user.name}</td>
                  <td>{user.email}</td>
                  <td>
                    <span className={`badge badge-${user.role}`}>
                      {user.role}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {pagination && (
            <div className="pagination">
              {Array.from({ length: pagination.totalPages }, (_, i) => (
                <Link
                  key={i + 1}
                  href={`/users?page=${i + 1}`}
                  className={pagination.currentPage === i + 1 ? 'active' : ''}
                  preserveScroll
                >
                  {i + 1}
                </Link>
              ))}
            </div>
          )}
        </div>

        <div className="card">
          <div className="card-header">
            Deferred Stats
            <button
              className="btn btn-primary"
              onClick={loadStats}
              disabled={loading}
              style={{ marginLeft: '1rem', fontSize: '0.75rem' }}
            >
              {loading ? 'Loading...' : 'Reload Stats'}
            </button>
          </div>
          {stats ? (
            <div className="prop-demo">
              <div className="prop-label">Total Users</div>
              <div className="prop-value">{stats.totalUsers}</div>
              <div className="prop-label" style={{ marginTop: '1rem' }}>Active Users</div>
              <div className="prop-value">{stats.activeUsers}</div>
              <div className="prop-label" style={{ marginTop: '1rem' }}>Loaded At</div>
              <div className="prop-value">{stats.loadedAt}</div>
            </div>
          ) : (
            <p>Stats loading...</p>
          )}
        </div>
      </div>
    </MainLayout>
  )
}

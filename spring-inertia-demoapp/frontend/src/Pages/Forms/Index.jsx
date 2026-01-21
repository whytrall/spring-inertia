import { Link, router } from '@inertiajs/react'
import MainLayout from '../../Layouts/MainLayout'

export default function FormsIndex({ contacts }) {
  const deleteContact = (id) => {
    if (confirm('Are you sure you want to delete this contact?')) {
      router.post('/forms/delete', { id })
    }
  }

  return (
    <MainLayout>
      <div className="page-header">
        <h1>Forms</h1>
        <p>Demonstrating form handling with validation</p>
        <Link href="/forms/create" className="btn btn-success" style={{ marginTop: '1rem' }}>
          Add New Contact
        </Link>
      </div>

      <div className="card">
        <div className="card-header">Contacts</div>
        {contacts?.length > 0 ? (
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Message</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {contacts.map((contact) => (
                <tr key={contact.id}>
                  <td>{contact.id}</td>
                  <td>{contact.name}</td>
                  <td>{contact.email}</td>
                  <td>{contact.message}</td>
                  <td>
                    <button
                      className="btn btn-danger"
                      onClick={() => deleteContact(contact.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>No contacts yet. Create one!</p>
        )}
      </div>
    </MainLayout>
  )
}

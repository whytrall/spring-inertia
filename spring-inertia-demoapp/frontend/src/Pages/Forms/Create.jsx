import { useForm, Link, usePage } from '@inertiajs/react'
import MainLayout from '../../Layouts/MainLayout'

export default function FormsCreate() {
  const { errors, old } = usePage().props

  const { data, setData, post, processing } = useForm({
    name: old?.name || '',
    email: old?.email || '',
    message: old?.message || ''
  })

  const submit = (e) => {
    e.preventDefault()
    post('/forms')
  }

  return (
    <MainLayout>
      <div className="page-header">
        <h1>Create Contact</h1>
        <p>Fill out the form below to add a new contact</p>
      </div>

      <div className="card" style={{ maxWidth: '600px' }}>
        <form onSubmit={submit}>
          <div className="form-group">
            <label htmlFor="name">Name</label>
            <input
              id="name"
              type="text"
              className="form-control"
              value={data.name}
              onChange={(e) => setData('name', e.target.value)}
            />
            {errors?.name && <div className="form-error">{errors.name}</div>}
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              className="form-control"
              value={data.email}
              onChange={(e) => setData('email', e.target.value)}
            />
            {errors?.email && <div className="form-error">{errors.email}</div>}
          </div>

          <div className="form-group">
            <label htmlFor="message">Message</label>
            <textarea
              id="message"
              className="form-control"
              value={data.message}
              onChange={(e) => setData('message', e.target.value)}
            />
            {errors?.message && <div className="form-error">{errors.message}</div>}
          </div>

          <div className="btn-group">
            <button type="submit" className="btn btn-success" disabled={processing}>
              {processing ? 'Saving...' : 'Save Contact'}
            </button>
            <Link href="/forms" className="btn btn-primary">
              Cancel
            </Link>
          </div>
        </form>
      </div>
    </MainLayout>
  )
}

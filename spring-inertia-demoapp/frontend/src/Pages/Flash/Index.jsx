import { router } from '@inertiajs/react'
import { useState } from 'react'
import MainLayout from '../../Layouts/MainLayout'

export default function FlashIndex({ description }) {
  const [customType, setCustomType] = useState('info')
  const [customMessage, setCustomMessage] = useState('')

  const triggerFlash = (type) => {
    router.post(`/flash/${type}`)
  }

  const triggerCustomFlash = (e) => {
    e.preventDefault()
    if (customMessage.trim()) {
      router.post('/flash/custom', {
        type: customType,
        message: customMessage
      })
      setCustomMessage('')
    }
  }

  return (
    <MainLayout>
      <div className="page-header">
        <h1>Flash Messages</h1>
        <p>{description}</p>
      </div>

      <div className="grid grid-2">
        <div className="card">
          <div className="card-header">Trigger Flash Messages</div>
          <div className="btn-group" style={{ marginBottom: '1rem' }}>
            <button className="btn btn-success" onClick={() => triggerFlash('success')}>
              Success
            </button>
            <button className="btn btn-danger" onClick={() => triggerFlash('error')}>
              Error
            </button>
            <button className="btn btn-info" onClick={() => triggerFlash('info')}>
              Info
            </button>
            <button className="btn btn-warning" onClick={() => triggerFlash('warning')}>
              Warning
            </button>
          </div>
          <button
            className="btn btn-primary"
            onClick={() => triggerFlash('multiple')}
            style={{ display: 'block', width: '100%' }}
          >
            Multiple Flash Messages
          </button>
        </div>

        <div className="card">
          <div className="card-header">Custom Flash Message</div>
          <form onSubmit={triggerCustomFlash}>
            <div className="form-group">
              <label>Type</label>
              <select
                className="form-control"
                value={customType}
                onChange={(e) => setCustomType(e.target.value)}
              >
                <option value="success">Success</option>
                <option value="error">Error</option>
                <option value="info">Info</option>
                <option value="warning">Warning</option>
              </select>
            </div>
            <div className="form-group">
              <label>Message</label>
              <input
                type="text"
                className="form-control"
                value={customMessage}
                onChange={(e) => setCustomMessage(e.target.value)}
                placeholder="Enter your message..."
              />
            </div>
            <button type="submit" className="btn btn-primary">
              Send Flash Message
            </button>
          </form>
        </div>
      </div>

      <div className="card" style={{ marginTop: '1rem' }}>
        <div className="card-header">How Flash Messages Work</div>
        <div className="code">
{`// In your controller
inertia.flash("success", "Operation completed!")

// After redirect, flash data is available in props
// and automatically cleared after being displayed`}
        </div>
      </div>
    </MainLayout>
  )
}

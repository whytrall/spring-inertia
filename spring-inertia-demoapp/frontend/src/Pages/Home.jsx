import MainLayout from '../Layouts/MainLayout'

export default function Home({ title, description, features }) {
  return (
    <MainLayout>
      <div className="page-header">
        <h1>{title}</h1>
        <p>{description}</p>
      </div>

      <div className="features">
        {features?.map((feature, index) => (
          <div key={index} className="feature">
            <h3>{feature.title}</h3>
            <p>{feature.description}</p>
          </div>
        ))}
      </div>
    </MainLayout>
  )
}

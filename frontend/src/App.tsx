import { useEffect, useState } from 'react'

function App() {
  const [status, setStatus] = useState('연결 확인 중...')

  useEffect(() => {
    fetch('/api/health')
      .then((response) => {
        if (!response.ok) {
          throw new Error('API 요청 실패')
        }

        return response.json()
      })
      .then((data) => {
        setStatus(data.status)
      })
      .catch(() => {
        setStatus('연결 실패')
      })
  }, [])

  return (
    <div>
      <h1>Public Facility Reservation</h1>
      <p>Backend status: {status}</p>
    </div>
  )
}

export default App
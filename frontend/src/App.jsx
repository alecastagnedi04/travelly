import { useEffect, useState } from 'react'

import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid
} from 'recharts'

import './App.css'


function App() {


  /*
   * =====================================================
   * ID DEL VIAGGIO
   * =====================================================
   *
   * Spring/Thymeleaf apre la dashboard con:
   *
   * /react-dashboard/index.html?tripId=123
   *
   * React recupera dinamicamente il tripId
   * dalla query string.
   */
  const searchParams =
    new URLSearchParams(
      window.location.search
    )


  const tripId =
    searchParams.get('tripId')



  const [dashboard, setDashboard] =
    useState(null)


  const [loading, setLoading] =
    useState(true)


  const [error, setError] =
    useState(null)



  /*
   * =====================================================
   * CARICAMENTO DATI
   * =====================================================
   */
  useEffect(() => {


    /*
     * Se qualcuno apre direttamente:
     *
     * /react-dashboard/index.html
     *
     * senza specificare il viaggio,
     * non facciamo una richiesta verso:
     *
     * /api/trips/null/...
     */
    if (!tripId) {


      setError(
        'Viaggio non specificato.'
      )


      setLoading(
        false
      )


      return
    }



    /*
     * AbortController permette di annullare
     * la richiesta se il componente viene
     * smontato prima della risposta.
     */
    const controller =
      new AbortController()



    fetch(
      `/api/trips/${tripId}/expense-dashboard`,
      {
        signal: controller.signal
      }
    )

      .then(response => {


        if (!response.ok) {


          throw new Error(
            'Errore nel caricamento della dashboard'
          )
        }


        return response.json()
      })

      .then(data => {


        setDashboard(
          data
        )


        setLoading(
          false
        )
      })

      .catch(error => {


        /*
         * Se la richiesta è stata annullata
         * volontariamente non mostriamo errori.
         */
        if (error.name === 'AbortError') {


          return
        }


        console.error(
          error
        )


        setError(
          error.message
        )


        setLoading(
          false
        )
      })



    /*
     * Cleanup dell'effect.
     */
    return () => {


      controller.abort()
    }


  }, [tripId])



  /*
   * =====================================================
   * CARICAMENTO
   * =====================================================
   */
  if (loading) {


    return (

      <div className="dashboard-page">


        <h1>
          Dashboard spese
        </h1>


        <p>
          Caricamento...
        </p>


      </div>
    )
  }



  /*
   * =====================================================
   * ERRORE
   * =====================================================
   */
  if (error) {


    return (

      <div className="dashboard-page">


        <h1>
          Dashboard spese
        </h1>


        <div className="error-box">

          {error}

        </div>


      </div>
    )
  }



  /*
   * Nessun dato ricevuto.
   */
  if (!dashboard) {


    return null
  }



  /*
   * =====================================================
   * DATI GRAFICI
   * =====================================================
   */

  const categoryData =
    dashboard.categories || []



  const participantData =
    (dashboard.participants || [])
      .filter(
        participant =>
          participant.balance !== null
      )



  /*
   * Palette utilizzata dal grafico
   * delle categorie.
   */
  const pieColors = [
    '#5f8178',
    '#c77b52',
    '#d7b377',
    '#5b7487',
    '#a35b4f',
    '#8d9b6a'
  ]



  return (

    <div className="dashboard-page">


      {/* ================================= */}
      {/* TITOLO */}
      {/* ================================= */}

      <div className="dashboard-header">


        <div>


          <h1>
            Riepilogo spese
          </h1>


          <p>
            Analisi economica del viaggio
          </p>


        </div>


      </div>



      {/* ================================= */}
      {/* RIEPILOGO GENERALE */}
      {/* ================================= */}

      <div className="summary-grid">


        <div className="summary-card">


          <span>
            Spese totali
          </span>


          <strong>

            {dashboard.totalExpenses.toFixed(2)} €

          </strong>


        </div>



        <div className="summary-card">


          <span>
            Spese da assegnare
          </span>


          <strong>

            {dashboard.unassignedExpenses.toFixed(2)} €

          </strong>


        </div>



        <div className="summary-card">


          <span>
            Divisione spese
          </span>


          <strong>

            {
              dashboard.settlementAvailable
                ? 'Disponibile'
                : 'Non disponibile'
            }

          </strong>


        </div>


      </div>



      {/* ================================= */}
      {/* GRAFICI */}
      {/* ================================= */}

      <div className="charts-grid">


        {/* ================================= */}
        {/* SPESE PER CATEGORIA */}
        {/* ================================= */}

        <section className="chart-card">


          <h2>
            Spese per categoria
          </h2>


          <p className="chart-description">

            Come è distribuito il budget del viaggio

          </p>



          {
            categoryData.length === 0
              ?

              <p>
                Nessuna spesa disponibile.
              </p>

              :

              <div className="pie-wrapper">


                <ResponsiveContainer
                  width="100%"
                  height={360}
                >


                  <PieChart>


                    {/*
                     * Torta inferiore utilizzata
                     * per creare un leggero
                     * effetto di profondità.
                     */}
                    <Pie
                      data={categoryData}
                      dataKey="amount"
                      nameKey="category"
                      cx="50%"
                      cy="54%"
                      outerRadius={115}
                      innerRadius={45}
                      fill="#315147"
                      stroke="none"
                      isAnimationActive={false}
                    />



                    {/*
                     * Torta principale.
                     */}
                    <Pie
                      data={categoryData}
                      dataKey="amount"
                      nameKey="category"
                      cx="50%"
                      cy="50%"
                      outerRadius={115}
                      innerRadius={45}
                      paddingAngle={2}
                      label={
                        entry =>
                          `${entry.category}`
                      }
                    >


                      {
                        categoryData.map(
                          (entry, index) => (


                            <Cell
                              key={
                                `cell-${entry.category}`
                              }
                              fill={
                                pieColors[
                                  index %
                                  pieColors.length
                                ]
                              }
                            />


                          )
                        )
                      }


                    </Pie>



                    <Tooltip
                      formatter={
                        value =>
                          [
                            `${Number(value).toFixed(2)} €`,
                            'Spesa'
                          ]
                      }
                    />


                  </PieChart>


                </ResponsiveContainer>


              </div>
          }



          {/* LEGENDA CATEGORIE */}

          <div className="category-list">


            {
              categoryData.map(
                (category, index) => (


                  <div
                    className="category-row"
                    key={category.category}
                  >


                    <span
                      className="category-dot"
                      style={{
                        backgroundColor:
                          pieColors[
                            index %
                            pieColors.length
                          ]
                      }}
                    />


                    <span>

                      {category.category}

                    </span>


                    <strong>

                      {category.amount.toFixed(2)} €

                    </strong>


                  </div>


                )
              )
            }


          </div>


        </section>



        {/* ================================= */}
        {/* SALDO PARTECIPANTI */}
        {/* ================================= */}

        <section className="chart-card">


          <h2>
            Saldo partecipanti
          </h2>


          <p className="chart-description">

            Chi deve pagare e chi deve ricevere

          </p>



          {
            dashboard.settlementAvailable
              ?

              <ResponsiveContainer
                width="100%"
                height={360}
              >


                <BarChart
                  data={participantData}
                  layout="vertical"
                  margin={{
                    top: 20,
                    right: 40,
                    left: 30,
                    bottom: 20
                  }}
                >


                  <CartesianGrid
                    strokeDasharray="3 3"
                  />


                  <XAxis
                    type="number"
                    tickFormatter={
                      value =>
                        `${value} €`
                    }
                  />


                  <YAxis
                    type="category"
                    dataKey="participantName"
                    width={90}
                  />


                  <Tooltip
                    formatter={
                      value =>
                        [
                          `${Number(value).toFixed(2)} €`,
                          'Saldo'
                        ]
                    }
                  />


                  <Bar
                    dataKey="balance"
                    fill="#5f8178"
                    radius={[
                      0,
                      6,
                      6,
                      0
                    ]}
                  />


                </BarChart>


              </ResponsiveContainer>

              :

              <div className="warning-box">

                {
                  dashboard.settlementMessage
                }

              </div>
          }


        </section>


      </div>



      {/* ================================= */}
      {/* PARTECIPANTI */}
      {/* ================================= */}

      <section className="participants-card">


        <h2>
          Riepilogo partecipanti
        </h2>



        <div className="participant-list">


          {
            (dashboard.participants || [])
              .map(
                participant => (


                  <div
                    className="participant-row"
                    key={participant.participantId}
                  >


                    <div>


                      <strong>

                        {
                          participant
                            .participantName
                        }

                      </strong>


                      <span>

                        Ha pagato:

                        {' '}

                        {
                          participant
                            .paidAmount
                            .toFixed(2)
                        }

                        €

                      </span>


                    </div>



                    {
                      participant.balance !== null
                      &&


                      <div
                        className={
                          participant.balance > 0
                            ? 'balance-positive'
                            :
                            participant.balance < 0
                              ? 'balance-negative'
                              : 'balance-zero'
                        }
                      >


                        {
                          participant.balance > 0
                            ?

                            `Deve ricevere ${participant.balance.toFixed(2)} €`

                            :

                            participant.balance < 0
                              ?

                              `Deve pagare ${Math.abs(participant.balance).toFixed(2)} €`

                              :

                              'In pari'
                        }


                      </div>
                    }


                  </div>


                )
              )
          }


        </div>


      </section>



      {/* ================================= */}
      {/* REGOLAMENTO SPESE */}
      {/* ================================= */}

      {
        dashboard.settlementAvailable
        &&


        <section className="settlement-card">


          <h2>
            Regolamento spese
          </h2>



          {
            (dashboard.settlements || [])
              .length === 0
              ?

              <p>

                Tutti i partecipanti sono già in pari.

              </p>

              :

              (dashboard.settlements || [])
                .map(
                  (settlement, index) => (


                    <div
                      className="settlement-row"
                      key={index}
                    >


                      <strong>

                        {
                          settlement
                            .fromParticipantName
                        }

                      </strong>


                      <span>
                        deve
                      </span>


                      <strong>

                        {
                          settlement
                            .amount
                            .toFixed(2)
                        }

                        €

                      </strong>


                      <span>
                        a
                      </span>


                      <strong>

                        {
                          settlement
                            .toParticipantName
                        }

                      </strong>


                    </div>


                  )
                )
          }


        </section>
      }


    </div>
  )
}


export default App
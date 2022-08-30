package grafana

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class DashboardTest extends Simulation {

  //val test_name = System.getProperty("test_name")
  val test_name = "tablerobase"
  val globalHeaders = Map(
    "Accept" -> "application/json",
    "Content-Type" -> "application/json"
  )
  val grafana = "http://localhost:3000"


  val httpProtocol1 = http
    .baseUrl(grafana)
    .basicAuth("admin","admin")

  val createDashboard= scenario("Crear Token")
    .exec(http(requestName= "getToken")
      .post("/api/auth/keys")
      .headers(globalHeaders)
      .body(ElFileBody("bodies/Dashboard/plantillaToken.json"))
      .check(bodyString.saveAs("tokenResponse"))
      .check(jsonPath("$.key").saveAs("token")))


    .exec{
      session => session.set("test_name", s"${test_name}")
    }
    .exec(
      http("post Crear Datasource")
        .post(s"${grafana}/api/datasources")
        .headers(globalHeaders)
        .header("Authorization" , "Bearer ${token}")
        .body(ElFileBody("bodies/Dashboard/plantillaSource.json"))
    )

    .exec(
      http("post Crear Paneles")
        .post(s"${grafana}/api/dashboards/db")
        .headers(globalHeaders)
        .header("Authorization" , "Bearer ${token}")
        .body(ElFileBody("bodies/Dashboard/plantillaDashboard.json"))
    )

  setUp(
    createDashboard.inject(atOnceUsers(1)).protocols(httpProtocol1)
  )
}

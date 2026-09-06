package paperweb

class PaperwebSuite extends munit.FunSuite:
  test("asset version is added as a query parameter") {
    assertEquals(
      Paperweb.versionAssetUrl("/assets/app.css", "1234"),
      "/assets/app.css?v=1234"
    )
  }

  test("asset version preserves an existing query and fragment") {
    assertEquals(
      Paperweb.versionAssetUrl("/assets/app.css?theme=dark#colors", "1234"),
      "/assets/app.css?theme=dark&v=1234#colors"
    )
  }

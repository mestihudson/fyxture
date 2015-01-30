import org.junit.Test
import org.yaml.snakeyaml.Yaml

class FyxtureClearTest {
  @Test void "clear"() {
    Yaml yaml = new Yaml()
    String document = """
common:
  datasource: h2

"""
    new File("config.yml").write(document)
    //println yaml.dump(yaml.load(document))
    // Fyxture.clear()
  }
}

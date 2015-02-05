import org.junit.Test
import org.junit.Ignore

import br.gov.frameworkdemoiselle.behave.controller.BehaveContext
import br.gov.frameworkdemoiselle.behave.internal.filter.StoryOrScenarioFilter

class FyxtureETest {
  private BehaveContext context = BehaveContext.getInstance()

  @Test void "all stories"() {
    context.setStepsPackage "steps"
    context.run "/stories", filter()
  }

  private StoryOrScenarioFilter filter() {
    String value = System.getProperty "filter"
    String type = System.getProperty "filterType"
    return value == null ? null : (type != null && type.equals("story") ? StoryOrScenarioFilter.story(value) : StoryOrScenarioFilter.scenario(value))
  }
}

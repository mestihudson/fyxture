package steps

import org.jbehave.core.annotations.Given
import org.jbehave.core.annotations.Then
import org.jbehave.core.annotations.When

import br.gov.frameworkdemoiselle.behave.parser.jbehave.CommonSteps

class AutenticacaoSteps extends CommonSteps {
  @Given("eu não estou autenticado")
  def "eu não estou autenticado"() {}

  @When("eu tento entrar na página principal")
  def "eu tento entrar na página principal"() {}

  @Then("eu sou encaminhado para página de autenticação")
  def "eu sou encaminhado para página de autenticação"() {}
}

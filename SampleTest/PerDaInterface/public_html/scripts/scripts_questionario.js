/* Leitura dos ficheiros:
 * - Resumo e Resultado
 * 
 * @returns {undefined}
 */
function loadCharts() {
    $.ajax({
        type: "GET",
        url: "Resumo.csv",
        dataType: "text",
        success: function (data) {
            processResumo(data);
            processData(data);
            processExtensao(data);
        }
    });

    $.ajax({
        type: "GET",
        url: "Resultado.csv",
        dataType: "text",
        success: function (data) {
            processResultado(data);

        }
    });

}
/* Leitura do ficheiro Resumo para gerar:
 * - Documentos analisados, total de documentos e densidade de risco;
 * - Classificação do Grau de Risco (em gráfico semi-circular)
 *  
 * @param {type} conteudo
 * @returns {undefined}
 */
function processResumo(conteudo) {
    var allTextLines = conteudo.split(/\r\n|\n/);

    var tipoDeDados;

    if (allTextLines[0].split(";").length == 9)
        tipoDeDados = "BD";
    else
        tipoDeDados = "FS";


    var densidadeRisco = 0;
    var risco = new Array();

    for (var r = 0; r < 6; r++) {
        risco.push({risco: r, quantidade: 0});
    }

    var auxRisco;
    
    if (tipoDeDados == "BD") {
        var auxTotalCampos = 0;
        var auxTotalColunas = 0;
        var auxCamposAnalisados = 0;
                              
        for(var i=1; i<allTextLines.length - 1; i++){
            auxCamposAnalisados += parseInt(allTextLines[i].split(";")[7]);
            auxTotalCampos += parseInt(allTextLines[i].split(";")[8]);
            auxTotalColunas += parseInt(allTextLines[i].split(";")[1]);
            auxRisco = allTextLines[i].split(";");
            densidadeRisco += parseFloat(auxRisco[6].replace(",", "."));
            risco[auxRisco[5]].quantidade++;
        }
        
        $("#totalDocs").html(allTextLines.length - 2);
        $("#totalDocsLabel").html("Número de Tabelas:");
        
        $("#tempoLocalizacao").html(auxTotalColunas);
        $("#tempoLocalizacaoLabel").html("Número de Colunas: ");
        
        $("#documentosAnalisados").html(auxCamposAnalisados);
        $("#documentosAnalisadosLabel").html("Campos Analisados:");

        $("#tempoAnalise").html(auxTotalCampos)
        $("#tempoAnaliseLabel").html("Total de Campos: ");
        
        $(".areaGraficoExtensao").html("");
        
    } else {
        $("#documentosAnalisados").html(allTextLines.length - 3);
        
        $("#totalDocs").html(allTextLines[allTextLines.length - 2].split(";")[1]);
        
        for (var i = 1; i < allTextLines.length - 2; i++) {
            auxRisco = allTextLines[i].split(";");
            densidadeRisco += parseFloat(auxRisco[7].replace(",", "."));
            risco[auxRisco[6]].quantidade++;
        }
        
        $("#tempoLocalizacao").html(allTextLines[allTextLines.length - 2].split(";")[3]);
        $("#tempoAnalise").html(allTextLines[allTextLines.length - 2].split(";")[5]);
    }

    $("#densidadeRisco").html(densidadeRisco.toFixed(2));

    var graphlabel = [];
    var graphdata = [];
    for (var i = 0; i < risco.length; i++) {
        graphlabel.push(risco[i].risco);
        graphdata.push(risco[i].quantidade);
    }

    new Chart(document.getElementById("chartRisco").getContext('2d'), {
        type: 'pie',
        options: {
            rotation: -Math.PI,
            cutoutPercentage: 40,
            circumference: Math.PI
        },

        data: {
            labels: graphlabel,
            datasets: [{
                    label: 'Classificação de Risco',
                    data: graphdata,
                    backgroundColor: [
                        'rgb(248, 248, 248)',
                        'rgb(189, 215, 238)',
                        'rgb(169, 208, 142)',
                        'rgb(255, 255, 0)',
                        'rgb(255, 192, 0)',
                        'rgb(255, 0, 0)'

                    ],
                    borderColor: [
                        'rgb(0, 0, 0)',
                        'rgb(0, 0, 0)',
                        'rgb(0, 0, 0)',
                        'rgb(0, 0, 0)',
                        'rgb(0, 0, 0)',
                        'rgb(0, 0, 0)'

                    ],
                    borderWidth: 0
                }]
        },
        options: {
            title: {
                display: true,
                position: 'top',
                text: 'Classificação de Risco'
            }
        }

    });
}



/* Leitura do ficheiro Resultado para geração:
 * - Do gráfico de barras com os tipos de entidades
 * 
 * @param {type} conteudo
 * @returns {undefined}
 */
function processResultado(conteudo) {

    /* agregação dos dados pelo tipo de entidade */
    var allTextLines = conteudo.split(/\r\n|\n/);
    var tiposEntidade = new Array();
    var auxTipoEntidade;


    for (var i = 1; i < allTextLines.length; i++) {
        auxTipoEntidade = allTextLines[i].split(";");
        var existe = false;
        for (var j = 0; j < tiposEntidade.length; j++) {
            if (tiposEntidade[j].entidade == auxTipoEntidade[3]) {
                existe = true;
                tiposEntidade[j].quantidade++;
                break;
            }
        }
        if (!existe)
            tiposEntidade.push({entidade: auxTipoEntidade[3], quantidade: 1});
    }

    // Definição das cores do gráfico
    var graphlabelTipoEntidade = [];
    var graphdataTipoEntidade = [];
    var bckColor = [];
    var brdColor = [];
    for (var i = 0; i < tiposEntidade.length - 1; i++) {
        graphlabelTipoEntidade.push(tiposEntidade[i].entidade);
        graphdataTipoEntidade.push(tiposEntidade[i].quantidade);
        var r = Math.floor(Math.random() * 256);
        var g = Math.floor(Math.random() * 256);
        var b = Math.floor(Math.random() * 256);

        bckColor.push('rgba(' + r + ', ' + g + ', ' + b + ', 0.2)');
        brdColor.push('rgba(' + r + ', ' + g + ', ' + b + ', 1)');
    }

    new Chart(document.getElementById("chartTipoEntidade").getContext('2d'), {
        type: 'bar',
        data: {
            labels: graphlabelTipoEntidade,
            datasets: [{
                    data: graphdataTipoEntidade,
                    backgroundColor: bckColor,
                    borderColor: brdColor
                }]
        },
        options: {
            legend: {
                display: false
            },
            scales: {
                yAxes: [{
                        ticks: {
                            beginAtZero: true
                        }
                    }]
            },
            title: {
                display: true,
                position: 'top',
                text: 'Tipo de Dados Descobertos'
            }
        }
    });
}


/* Leitura do ficheiro Resumo para gerar:
 * - Tabela do quandro resumo dos resultados
 * 
 * @param {type} conteudo
 * @returns {undefined}
 */
function processData(conteudo) {

    var allTextLines = conteudo.split(/\r\n|\n/);

    var headers = allTextLines[0].split(';');

    var lines = [];

    for (var i = 1; i < allTextLines.length; i++) {
        var data = allTextLines[i].split(';');
        if (data.length == headers.length) {

            var tarr = [];
            for (var j = 2; j < headers.length; j++) {
                tarr.push(data[j]);
            }
            lines.push(tarr);
        }
    }
    ;
    var graphlabel = [];
    var graphdata = [];
    for (var i = 0; i < lines.length; i++) {
        graphlabel.push(lines[i][3]);
        graphdata.push(lines[i][6]);
    }

 
    var table = document.getElementById("testetable");
    var header = table.insertRow(0);
    for (var j = 0; j < headers.length - 2; j++) {
        var cell = header.insertCell(j);
        cell.innerHTML = headers[j + 2];
    }
    for (var i = 0; i < lines.length; i++) {
        var row = table.insertRow(i + 1);
        for (var j = 0; j < lines[i].length; j++) {
            var cell = row.insertCell(j);
            cell.innerHTML = lines[i][j];
        }
    }

}

function geraRelatorio() {

    /*validacao*/

    var countRespostas = 0;
    $(".pergunta").css("color", "#000000");
    $(".pergunta").each(function () {
        if ($("[name=" + $(this).attr('id') + "]:checked").length === 0) {
            $(this).css("color", "red");
        } else {
            countRespostas++;
        }

    });

    if (countRespostas !== $(".pergunta").length) {
        $(".errorMessage").html("Atenção!!! Responda a todas as perguntas...");
        return;
    }


    /*geracao do relatorio*/

    $(".subtitle").html("Relatório final de Avaliação");

    $("#btGerarRelatorio").css("display", "none");

    $(".firstArea, .secondArea, #testetable").css("display", "block");
    $(".coluna.esquerda").css("width", "40%");
    $(".coluna.direita").css("width", "60%");

    var conformes = 0;
    var naoConformes = 0;
    $(".pergunta").each(function () {
        if ($("[name=" + $(this).attr('id') + "]:checked").val() != "0") {
            naoConformes++;
            $(this).addClass("perguntaNaoConforme");
            var result = "<div class='naoConforme'>Não conforme.</div><div class='planoMitigacao'>";
            switch ($(this).attr('id')) {
                case 'questao1':
                    result += "Independentemente do sistema operativo utilizado e/ou aplicação, deve garantir de todos os acessos são baseados em protocolos de autenticação. O mais simples e económico é baseado num segredo partilhado (através do uso de palavra-passe).";
                    break;
                case 'questao2':
                    result += "Mediante o nível protocolar deverá escolher o mecanismo mais adequado para garantir a integridade e confidencialidade das comunicações. Possíveis soluções: PGP, PEM; SSH, HTTP, IMAPS, POPS, TLS/SSL; IPSEC; GSM, WEP, Bluetooth).";
                    break;
                case 'questao3':
                    result += "Deverá garantir e forçar nas politicas de gestão para que os utilizadores sejam obrigados a ter uma senha que seja complexa com um mínimo de 9 caracteres. Para ser fácil de memorizar recomenda-se a utilização de uma pequena frase com alterações de alguns caracteres para símbolos e números.";
                    break;
                case 'questao4':
                    result += "Deverá garantir e forçar nas politicas de gestão para que os administradores possuem uma senha complexa com um mínimo de 13 caracteres. Para ser fácil de memorizar recomenda-se a utilização de uma pequena frase com alterações de alguns caracteres para símbolos e números.";
                    break;
                case 'questao5':
                    result += "Recomenda-se a implementação de um sistema multimétodo, no mínimo, para os administradores de rede e sistemas. Note-se que os elementos de prova usados nos protocolos de autenticação são baseados (1) no que se sabe – ex: senha, (2) o que se possui – ex: cartão ou telemóvel e (3) o que se é – ex: impressão digital. Combine mais do que um elemento.";
                    break;
                case 'questao6':
                    result += "Para além de incentivar o bloqueio por parte dos utilizadores quando abandonem a estação de trabalho, force as politicas de segurança da infraestrutura para bloquear automaticamente após 5 minutos, no máximo.";
                    break;
                case 'questao7':
                    result += "Crie e utilize contas de utilizador institucionais em detrimento de contas pessoais e defina os privilégios de acordo com a função e a necessidade de executar.";
                    break;
                case 'questao8':
                    result += "Edifique uma matriz de funções e o nível de privilegio e implemente na politicas da sua organização. Procure garantir se um determinado utilizador desempenhe duas funções distintas que possua duas contas e utiliza as contas de acordo com a função. Principio dos privilégios mínimos.";
                    break;
                case 'questao9':
                    result += "Procure garantir que os utilizadores com acesso ao tratamento de dados pessoais possuam uma estação de trabalho própria e que não seja partilhada com outro utilizador.";
                    break;
                case 'questao10':
                    result += "Garanta que todos os computadores que tenham acesso e tratem dados pessoais possuem configurações estáticas de configuração de rede (IP fixo), configuração da porta de rede dedicada exclusivamente à máquina (através do MAC Address).";
                    break;
                case 'questao11':
                    result += "Procure ter um sistema centralizado de registos (SIEM – Security Information and Event Management) e no caso de tratar dados pessoais de acordo com o artigo 9.º configure o sistema e/ou aplicações para promoverem todos os registos dos eventos relacionados com o tratamento de dados (acesso, alteração, transferência, remoção). Nomeadamente informação de quem acedeu, de onde acedeu, quando acedeu, a que dados acedeu e que ação foi efetuada.";
                    break;
                case 'questao12':
                    result += "Garanta que possui mecanismos para conseguir auditar regularmente todas as contas dos utilizadores que tratem dados pessoais são com uma periodicidade mínima de 180 dias para os utilizadores normais e 90 para os administradores?";
                    break;
                case 'questao13':
                    result += "Avalie se todos os sistemas e aplicações possuem mecanismos automáticos de alarmística para as contas de utilizadores sem atividade. Tenha especialmente atenção aos sistemas legados mais antigos. Em caso afirmativo, avalie bem os riscos e pondere evoluir para um sistema atual.";
                    break;
                case 'questao14':
                    result += "Force o registo dos eventos de atividades relacionadas com o tratamento de dados apenas ao modo de leitura e garanta que apenas sejam acessíveis por intermédio de dupla autenticação (duas pessoas distintas) para os assinar digitalmente. A periodicidade de deverá ser reajustada de acordo com o nível de criticidade dos dados.";
                    break;
                case 'questao15':
                    result += "Defina as politicas de gestão dos controlos de acesso para se registar todas os processos de autenticação, incluindo as tentativas falhadas.";
                    break;
                case 'questao16':
                    result += "Deve procurar garantir que os registos das atividades (logs) relacionadas com o tratamento de dados pessoais contenham, preferencialmente, o endereço de acesso (IP e porto), hostname, hash da conta do utilizador, registo do dia e hora ao segundo e a ação efetuada. Quando se trate de dados do artigo 9.º esta ação é mandatória.";
                    break;
                case 'questao17':
                    result += "Para qualquer transferência de dados pessoais deve garantir a utilização de mecanismos de segurança adequados, sendo que (1) para dados críticos (referentes ao artigo 9.º do RGPD) os dados devem estar cifrados; (2) para os restantes dados poderá adotar outros mecanismos como o mascaramento, anonimização e/ou pseudonimização. Em complemento, recomenda-se a utilização de VPN para garantir a identidade correta do remetente e destinatário.";
                    break;
                case 'questao18':
                    result += "Tendo em conta a complexidade e custo desta medida, o sistema de backup deve, no mínimo, estar segregado logicamente, minimizando o seu acesso a um número mínimo de utilizadores. Recomendando-se que (1) apenas os sistemas que tratam dados pessoais críticos estejam devidamente cifrados; (2) em relação aos restantes dados pessoais, deve haver registos de todas as atividades. Independentemente da solução deverá, no mínimo, avaliar os registos de logs mensalmente. Faça uma avaliação de impacto para conhecer o risco.";
                    break;
                case 'questao19':
                    result += "Promova um inventário com todos os sistemas de informação que utiliza diferenciando os que são destinados exclusivamente para o tratamento de dados pessoais, classifique a criticidade dos dados pessoais, enumere o número de utilizadores com acesso aos dados e analise o fluxo do processo de tratamento dos dados pessoais.";
                    break;
                case 'questao20':
                    result += "Garanta que possui o vínculo contratual ou termo de consentimento por parte do titular de dados de todos os dados pessoais que trata e relacione-o com o sistema e/ou aplicações responsáveis pelo tratamento de dados.";
                    break;
                case 'questao21':
                    result += "Dê formação e crie incentivos para todos os colaboradores com responsabilidade no processo de recolha e tratamento dos dados pessoais (nomeadamente sobre segurança de proteção e privacidade de dados). Promova regularmente, treino e avaliação de situações que pretende testar (procedimentos sobre o processo de tratamento de dados institucionalizado).";
                    break;
                case 'questao22':
                    result += "Para além das qualificações necessária para os elementos com funções no processo de tratamento de dados, procure promover ações internas sobre as politicas e regulamentos institucionalizados na organização.";
                    break;
                case 'questao23':
                    result += "Elabore rapidamente o código de conduta e envolva a direção no processo para aprovar e divulgar quer pelos colaboradores quer para os titulares dos dados. Procure ser o mais transparente na divulgação do código de conduta de modo a garantir uma maior lealdade e compromisso.";
                    break;
                case 'questao24':
                    result += "Procure ter espaços técnicos dedicados para acomodar os ativos da infraestrutura tecnológica, limitando o acesso a um número restrito de elementos, devidamente identificados. Evite ter equipamentos ativos de rede acessíveis (fisicamente e logicamente) a qualquer elemento.";
                    break;
                case 'questao25':
                    result += "Implemente um sistema de controlo de acessos aos espaços físicos (técnicos e de trabalho) e preferencialmente, instale um sistema de vídeo como complemento à monitorização dos espaços técnicos.";
                    break;
                case 'questao26':
                    result += "Condicione o acesso físico e logico aos repositórios de dados (com especial enfase aos dados pessoais) e garanta mecanismos de controlo de acesso e monitorização dos espaços.";
                    break;
                case 'questao27':
                    result += "Procure na medida do possível implementar aplicações (front-end) para aceder aos repositórios garantindo que a aplicações promova autenticações seguras e registe todas os eventos das atividades realizadas. Adicionalmente, deverá assegurar mecanismos de proteção de acordo com o tipo de utilizador e função, ou seja, é nesta componente que são aplicadas as medidas de proteção de acordo com a área de negócio (pseudonização, anonimização e/ou cifragem).";
                    break;
                case 'questao28':
                    result += "Anule de imediato acessos diretos. No caso de não ser possível e/ou viável restrinja ao máximo o número de utilizadores que possam aceder diretamente aos repositórios sem ser por intermédio de aplicação dedicada para o efeito. Tenha permanentemente presente esta situação e implemente o mais rápido possível uma forma de monitorizar e ter todos os registos das ações/atividades desenvolvidas (seja auditável).";
                    break;
                case 'questao29':
                    result += "Está perante uma situação de alto risco, principalmente se trata dados pessoais considerados sensíveis. Implemente o mais rápido possível uma forma de monitorizar e ter todos os registos das ações/atividades desenvolvidas para tornar o sistema auditável.";
                    break;
                case 'questao30':
                    result += "No caso de já possuir sistemas e/ou aplicações que façam o controlo automatizo parametrize e crie regras para promover alertas regulares. No caso do controlo ser manual não facilite, mantenha permanentemente atualizado os perfis de utilizadores e sempre que se verificar uma alteração de funções atualize de imediato.";
                    break;
                default:
                    break;
            }
            result += "</div>";
            result += "<div class=\"avaliacaoRisco\"><span>Impacto</span>: 1 ";
            result += "<input type=\"radio\" name=\"impacto" + $(this).attr('id').substring(7, 9) + "\" value=\"0.05\">";
            result += ",  2 ";
            result += "<input type=\"radio\" name=\"impacto" + $(this).attr('id').substring(7, 9) + "\" value=\"0.1\">";
            result += ",  3 ";
            result += "<input type=\"radio\" name=\"impacto" + $(this).attr('id').substring(7, 9) + "\" value=\"0.2\">";
            result += ",  4 ";
            result += "<input type=\"radio\" name=\"impacto" + $(this).attr('id').substring(7, 9) + "\" value=\"0.4\">";
            result += ",  5 ";
            result += "<input type=\"radio\" name=\"impacto" + $(this).attr('id').substring(7, 9) + "\" value=\"0.8\">";
            result += "</div>";

            result += "<div class=\"avaliacaoRisco\"><span>Probabilidade</span>: 1 ";
            result += "<input type=\"radio\" name=\"probabilidade" + $(this).attr('id').substring(7, 9) + "\" value=\"0.1\">";
            result += ",   2 ";
            result += "<input type=\"radio\" name=\"probabilidade" + $(this).attr('id').substring(7, 9) + "\" value=\"0.3\">";
            result += ",   3 ";
            result += "<input type=\"radio\" name=\"probabilidade" + $(this).attr('id').substring(7, 9) + "\" value=\"0.5\">";
            result += ",   4 ";
            result += "<input type=\"radio\" name=\"probabilidade" + $(this).attr('id').substring(7, 9) + "\" value=\"0.7\">";
            result += ",   5 ";
            result += "<input type=\"radio\" name=\"probabilidade" + $(this).attr('id').substring(7, 9) + "\" value=\"0.9\">";
            result += "</div>";


            $("[name=" + $(this).attr('id') + "]:checked").parent().html(result);

        } else {
            conformes++;
            $(this).css("display", "none");
            $("[name=" + $(this).attr('id') + "]:checked").parent().css("display", "none");
        }

    });

    $(".coluna.esquerda").append("\n<div class='btAvRisco' onclick='javascript:obterAvaliacaoRisco();'>Carregar aqui para obter avaliação de risco</div>");

    var stringConformidade = "<div class='areaConformidade'>";

    stringConformidade += "<div class='titleConformidade'>Conformes: </div><div class='valueConformidade' id='conforme'>" + conformes + "</div>";
    stringConformidade += "<div class='titleConformidade'>Não conformes: </div><div class='valueConformidade' id='naoConforme'>" + naoConformes + "</div>";
    stringConformidade += "<div class='titleConformidade'>Rácio de conformidade: </div><div class='valueConformidade' id='racioConformidade'>" + conformes / (conformes + naoConformes).toFixed(2) + "</div>";
    stringConformidade += "</div>";

    $(".coluna.esquerda").append(stringConformidade);

    var stringGrauRisco = "<div class='areaRisco'>";
    stringGrauRisco += "Risco: ";

    if ($("#racioConformidade").text() == 0) {
        stringGrauRisco += "<span style='color:#ff0000; font-size: 40px;'>Máximo</span>";
    } else {
        var auxRisco = ($("#densidadeRisco").text() / $("#racioConformidade").text()).toFixed(2);
        if (auxRisco > 1)
            stringGrauRisco += "<span style='color:#ff0000; font-size: 40px;'>Máximo</span>";
        else if (auxRisco > 0.9)
            stringGrauRisco += "<span style='color:#ff0000;'>" + auxRisco + "</span>";
        else if (auxRisco > 0.7)
            stringGrauRisco += "<span style='color:#ff9800;'>" + auxRisco + "</span>";
        else if (auxRisco > 0.5)
            stringGrauRisco += "<span style='color:rgba(180, 102, 0, 1);'>" + auxRisco + "</span>";
        else if (auxRisco > 0.2)
            stringGrauRisco += "<span style='color:rgba(90, 255, 64, 1);'>" + auxRisco + "</span>";
        else
            stringGrauRisco += "<span style='color:rgba(0, 255, 0, 1);'>" + auxRisco + "</span>";
    }
    stringGrauRisco += "</div>";
    $(".totalDocumentos:last-child").append(stringGrauRisco);


    gerarChartConformidade();

    $(".errorMessage").css("display", "none");
    $(".tituloQuestionario").css("display", "block");

}


function gerarChartConformidade() {
    new Chart(document.getElementById("chartConformidade").getContext('2d'), {
        type: 'pie',
        data: {
            labels: ['Conforme', 'Não Conforme'],
            datasets: [{
                    label: 'Grau de Conformidade',
                    data: [$("#conforme").text(), $("#naoConforme").text()],
                    backgroundColor: [
                        'rgba(0, 255, 0, 0.2)',
                        'rgba(255, 0, 0, 0.2)'

                    ],
                    borderColor: [
                        'rgba(0, 255, 0, 1)',
                        'rgba(255, 0, 0, 1)'

                    ],
                    borderWidth: 1
                }]
        },
        options: {
            title: {
                display: true,
                position: 'top',
                text: 'Grau de Conformidade'
            }
        }
    });
}


function obterAvaliacaoRisco() {
    var totalAvaliacaoRisco = 0;
    $('.perguntaNaoConforme').each(
            function () {
                totalAvaliacaoRisco += (1 * parseFloat($("[name='impacto" + $(this).attr('id').substring(7, 9) + "']:checked").val()) * parseFloat($("[name='probabilidade" + $(this).attr('id').substring(7, 9) + "']:checked").val()));
            });
    alert(parseFloat(totalAvaliacaoRisco) / ($('.naoConforme').length)).toFixed(2);
}
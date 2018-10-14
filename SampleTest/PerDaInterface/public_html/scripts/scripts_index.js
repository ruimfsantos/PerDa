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

    });
}


function processExtensao(conteudo) {
    /* agregação dos dados pela extensão */
    var allTextLines = conteudo.split(/\r\n|\n/);
    var extensao = new Array();
    var aux;

    for (var i = 1; i < allTextLines.length - 1; i++) {
        aux = allTextLines[i].split(";");
        var existe = false;
        for (var j = 0; j < extensao.length; j++) {
            if (extensao[j].entidade == aux[2]) {
                existe = true;
                extensao[j].quantidade++;
                break;
            }
        }
        if (!existe)
            extensao.push({entidade: aux[2], quantidade: 1});
    }

    // Definição das cores do gráfico
    var graphlabel = [];
    var graphdata = [];
    var bckColor = [];
    var brdColor = [];
    for (var i = 0; i < extensao.length - 1; i++) {
        graphlabel.push(extensao[i].entidade);
        graphdata.push(extensao[i].quantidade);
        var r = Math.floor(Math.random() * 256);
        var g = Math.floor(Math.random() * 256);
        var b = Math.floor(Math.random() * 256);

        bckColor.push('rgba(' + r + ', ' + g + ', ' + b + ', 0.2)');
        brdColor.push('rgba(' + r + ', ' + g + ', ' + b + ', 1)');
    }

    /* definição e impressão do gráfico */
    new Chart(document.getElementById("chartExtensao").getContext('2d'), {
        type: 'horizontalBar',
        data: {
            labels: graphlabel,
            datasets: [{
                    data: graphdata,
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

    /* definição e impressão do gráfico */
    new Chart(document.getElementById("chartTipoEntidade").getContext('2d'), {
        type: 'horizontalBar',
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

/* função de geração do relatório de avaliação dos resultados e validaçaõ das questões
 * 
 * @returns {undefined}
 */
function geraRelatorio() {

    /*validacao*/
    var countRespostas = 0;

    $(".pergunta").each(function () {
        if ($("[name=" + $(this).attr('id') + "]:checked").length === 0) {
            $(this).css("color", "red");
        } else {
            countRespostas++;
        }
    });

    if (countRespostas !== $(".pergunta").length) {
        $(".errorMessage").html("Atenção... Responda a todas as perguntas!!!!");
        return;
    }

}
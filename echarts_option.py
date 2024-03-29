option_pie = {
        "legend": {},
        "tooltip": {
            "trigger": 'axis',
            "showContent": "false"
        },
        "dataset": {
            "source": [
                ['', '2023'],
                ['非克隆代码段', 1],
                ['轻度克隆代码段', 2],
                ['中度克隆代码段', 3],
                ['高度克隆代码段', 4]
            ]
        },
        "series": [
            {
                "type": 'pie',
                "id": 'pie',
                "radius": ['40%', '75%'],
                #"center": ['50%', '30%'],
                "emphasis": {"focus": 'data',
                            "fontSize": '20',
                            "fontWeight": 'bold'},
                "label": {
                    "formatter": '{b}: {@2023} ({d}%)'
                },
            }
        ],
            "tooltip": {
                    "show": "true",
                },
            "label": {
                "show":"true"
    },
    }


option_gauge = {
        "tooltip": {
            "formatter": '{a} <br/>{b} : {c}%'
        },
        "series": [{
            "name": '克隆代码占比',
            "type": 'gauge',
            "startAngle": 180,
            "endAngle": 0,
            "progress": {
                "show": "true"
            },
            "radius":'100%', 

            "itemStyle": {
                "color": '#58D9F9',
                "shadowColor": 'rgba(0,138,255,0.45)',
                "shadowBlur": 10,
                "shadowOffsetX": 2,
                "shadowOffsetY": 2,
                "radius": '55%',
            },
            "progress": {
                "show": "true",
                "roundCap": "true",
                "width": 15
            },
            "pointer": {
                "length": '60%',
                "width": 8,
                "offsetCenter": [0, '5%']
            },
            "detail": {
                "valueAnimation": "true",
                "formatter": '{value}%',
                "backgroundColor": '#58D9F9',
                "borderColor": '#999',
                "borderWidth": 4,
                "width": '60%',
                "lineHeight": 20,
                "height": 20,
                "borderRadius": 188,
                "offsetCenter": [0, '40%'],
                "valueAnimation": "true",
            },
            "data": [{
                "value": 0,
                "name": '检测速率'
            }]
        }]
    }

option_bar = {
        "toolbox": {
        "show": "true",
        "feature": {
          "dataZoom": {
            "yAxisIndex": "none"
          },
          "dataView": {
            "readOnly": "false"
          },
          "magicType": {
            "type": ["line", "bar"]
          },
          "restore": {"show":"true"},
        }
      },
        "xAxis": {
            "type": 'category',
            "data": ['50 ~ 70%', '70 ~ 90%', '90 ~ 100%']
        },
        "yAxis": {
            "type": 'value'
        },
        "series": [{
            "data": [
                {"value":0, "itemStyle":{"color":"#00FF00"}},
                {"value":0, "itemStyle":{"color":"#FFFF00"}},
                {"value":0, "itemStyle":{"color":"#FF7D00"}},
                {"value":0, "itemStyle":{"color":"#FF0000"}}, 
                ],
            "type": 'bar'

        }],
        "tooltip": {
                        "show": "true"
                    },
        "label": {
            "show":"true"
        },
        
                        
        }

option_pie2 = {
        "legend": {},
        "tooltip": {
            "trigger": 'axis',
            "showContent": "false"
        },
        "dataset": {
            "source": [
                ['', '2023'],
                ['轻度克隆代码段', 2],
                ['中度克隆代码段', 3],
                ['高度克隆代码段', 4]
            ]
        },
        "series": [
            {
                "type": 'pie',
                "id": 'pie',
                "radius": ['40%', '75%'],
                #"center": ['50%', '30%'],
                "emphasis": {"focus": 'data',
                            "fontSize": '20',
                            "fontWeight": 'bold'},
                "label": {
                    "formatter": '{b}: {@2023} ({d}%)'
                },
            }
        ],
            "tooltip": {
                    "show": "true",
                },
            "label": {
                "show":"true"
    },
    }


option_pie3 = {
        "legend": {},
        "tooltip": {
            "trigger": 'axis',
            "showContent": "false"
        },
        "dataset": {
            "source": [
                ['', '2023'],
                ['正常文件', 2],
                ['可疑文件', 3]
            ]
        },
        "series": [
            {
                "type": 'pie',
                "id": 'pie',
                "radius": ['40%', '75%'],
                #"center": ['50%', '30%'],
                "emphasis": {"focus": 'data',
                            "fontSize": '20',
                            "fontWeight": 'bold'},
                "label": {
                    "formatter": '{b}: {@2023} ({d}%)'
                },
            }
        ],
            "tooltip": {
                    "show": "true",
                },
            "label": {
                "show":"true"
    },
    }


option_bar_cwe = {
        "toolbox": {
        "show": "true",
        "feature": {
          "dataZoom": {
            "yAxisIndex": "none"
          },
          "dataView": {
            "readOnly": "false"
          },
          "magicType": {
            "type": ["line", "bar"]
          },
          "restore": {"show":"true"},
        }
      },
        "xAxis": {
            "type": 'category',
            "data": []
        },
        "yAxis": {
            "type": 'value'
        },
        "series": [{
            "data": [
                {"value":0, "itemStyle":{"color":"#00FF00"}},
                {"value":0, "itemStyle":{"color":"#FFFF00"}},
                {"value":0, "itemStyle":{"color":"#FF7D00"}},
                {"value":0, "itemStyle":{"color":"#FF0000"}}, 
                ],
            "type": 'bar'

        }],
        "tooltip": {
                        "show": "true"
                    },
        "label": {
            "show":"true"
        },
        
                        
        }
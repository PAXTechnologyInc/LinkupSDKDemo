import requests
import sys

def get_metrics(project: str, sonar_home: str) -> dict:
    session = requests.Session()
    data = {'login': 'admin', 'password': 'Pax@123456'}
    loginUrl = sonar_home + "/api/authentication/login"
    session.post(loginUrl, data=data)
    url = sonar_home + "/api/measures/component"
    metricKeys = "alert_status,bugs,vulnerabilities,code_smells"
    params = {"metricKeys": metricKeys, "component": project}
    response = session.get(url=url, params=params)
    measures = response.json()
    metrics = measures.get('component').get('measures')
    dic = {}
    for metric in metrics:
        dic[metric.get('metric')] = metric.get('value')
    return dic

def get_dt_uuid(dt_home: str, project: str, version: str, token: str) -> str:
    url = dt_home + "/api/v1/project/lookup"
    params = {"name": project, "version": version}
    headers = {"X-Api-Key": token}
    response = requests.get(url=url, params=params, headers=headers)
    uuid = response.json().get("uuid")
    return uuid

def get_dt(uuid: str, dt_home: str, token: str) -> dict:
    url = dt_home + "/api/v1/metrics/project/" + uuid + "/current"
    headers = {"X-Api-Key": token}
    response = requests.get(url=url, headers=headers)
    metrics = response.json()
    dic = {}
    dic['components'] = metrics.get('components')
    dic['critical'] = metrics.get('critical')
    dic['high'] = metrics.get('high')
    dic['medium'] = metrics.get('medium')
    dic['low'] = metrics.get('low')
    dic['dt_vulnerabilities'] = metrics.get('vulnerabilities')
    return dic

def generate_html(metrics: dict, dt: dict, project: str, uuid: str):
    with open('develop_template.html', 'r', encoding="UTF-8") as f1:
        html = f1.read()
        for metric in metrics.keys():
            html = html.replace('#'+metric, metrics.get(metric))
        for d in dt.keys():
            html = html.replace('#'+d, str(dt.get(d)))
        html = html.replace('#uuid', uuid)
        sonar_url = "http://172.16.2.102:9000/dashboard?id=" + project
        dt_url = "http://172.16.2.25:8082/projects/" + uuid
        html = html.replace('#sonar_url', sonar_url)
        html = html.replace('#dt_url', dt_url)
        with open('develop.html', 'w', encoding="UTF-8") as f2:
            f2.write(html)

def main():
    args = sys.argv
    if len(args) < 3:
        sys.stderr.write("输入的参数不足，请输入project和version\n")
        exit(1)
    project = args[1]
    version = args[2]
    sonar_home = "http://172.16.2.102:9000"
    dt_home = "http://172.16.2.25:8083"
    dt_token = "HX04pMRIfKBlw8Tt4gYyxfYmr5VllClc"
    metrics = get_metrics(project=project, sonar_home=sonar_home)
    uuid = get_dt_uuid(dt_home=dt_home, project=project, version=version, token=dt_token)
    dt = get_dt(dt_home=dt_home, uuid=uuid, token=dt_token)
    generate_html(metrics, dt, project, uuid)
    # generate_html(metrics, None, project, None)
    sys.stdout.write("生成HTML成功！\n")

if __name__ == "__main__":
    main()

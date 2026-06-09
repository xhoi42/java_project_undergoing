import urllib.request
import urllib.error

versions = ['8.1.22','8.1.20','8.1.15','8.1.5','8.1.1','8.0.38','8.0.37','8.0.36','8.0.35','8.0.34']
for version in versions:
    url = f'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/{version}/mysql-connector-j-{version}.pom'
    try:
        with urllib.request.urlopen(url):
            print(version, 'exists')
    except urllib.error.HTTPError as e:
        print(version, 'missing', e.code)

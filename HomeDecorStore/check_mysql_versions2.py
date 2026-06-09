import urllib.request
import urllib.error

versions = ['8.1.0','8.1.1','8.0.34','8.0.33','8.0.32']
for version in versions:
    url = f'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/{version}/mysql-connector-j-{version}.pom'
    try:
        with urllib.request.urlopen(url):
            print(version, 'exists')
    except urllib.error.HTTPError as e:
        print(version, 'missing', e.code)

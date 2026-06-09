import re
import urllib.request

url = 'https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/3.2.0/spring-boot-dependencies-3.2.0.pom'
text = urllib.request.urlopen(url).read().decode('utf-8')
props = dict(re.findall(r'<([^>]+)>([^<]+)</\1>', text))
for g, a, v in [
    ('com.mysql', 'mysql-connector-j', 'mysql.version'),
    ('org.projectlombok', 'lombok', 'lombok.version'),
    ('org.springframework.boot', 'spring-boot-starter-web', '3.2.0'),
    ('org.springframework.boot', 'spring-boot-starter-data-jpa', '3.2.0'),
    ('org.springframework.boot', 'spring-boot-starter-validation', '3.2.0'),
    ('org.springframework.boot', 'spring-boot-starter-test', '3.2.0'),
]:
    version = props.get(v, v)
    print(f'{g}:{a}:{version}')

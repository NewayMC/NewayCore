# NewayCore

[![JitPack](https://jitpack.io/v/NewayMC/NewayCore.svg)](https://jitpack.io/#NewayMC/NewayCore)
[![CodeFactor](https://www.codefactor.io/repository/github/newaymc/newaycore/badge)](https://www.codefactor.io/repository/github/newaymc/newaycore)

Многофункциональная библиотека ( сборная солянка из всего и вся :/ ) для проектов NewayMC Studios

## Структура ( см. src )
- `ai` - ИИ для сущностей с огнестрельным оружием из TaCZ
- `files` - взаимодействия с файлами ( архивирование zstd и база данных H2 )
- `worlds` - датаген для измерений + их загрузчик

## Использование
Может отдельно потребоваться [TaCZ 1.21.1 NeoForge Port](https://github.com/MUKSC/TACZ-1.21.1)
```groovy
repositories {
    mavenCentral()
	maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.NewayMC:NewayCore:26.5.4'
}
```

## Лицензия
Apache 2.0 License
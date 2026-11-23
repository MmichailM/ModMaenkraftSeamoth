# Seamoth Mod (Fabric)

Мод для Minecraft на загрузчике **Fabric**, добавляющий управляемый подводный аппарат **Seamoth** («Мотылёк») с собственной 3D-моделью, системой питания, модулем хранилища, инструментом ремонта, кастомным GUI и клиент-серверной синхронизацией.

## Основные возможности

- **Транспорт Seamoth (`SeamothEntity`):** кастомная сущность с собственной 3D-моделью (`SeamothModel`), рендерером (`SeamothEntityRenderer`) и состоянием рендера (`SeamothRenderState`).
- **Система энергии и модулей:**
  - элементы питания — батареи (`BatteryItem`) и энергоячейки (`PowerCellItem`), поддерживающие расширение через теги предметов (`data/seamoth/tags/item/`);
  - модуль хранилища (`StorageModuleItem`) и собственный интерфейс инвентаря/управления (`SeamothMenu`, `SeamothScreen`);
  - инструмент ремонта (`RepairToolItem`) и предмет для размещения аппарата (`SeamothItem`).
- **Клиент-серверное взаимодействие:** кастомные сетевые пакеты для передачи управления (`SeamothInputPacket`), открытия интерфейса (`SeamothOpenInventoryPacket`) и синхронизации состояния (`SeamothSyncPacket`).
- **Камера, управление и механики:**
  - настраиваемые клавиши управления (`SeamothKeybindings`);
  - модификация камеры, наклона, рендера игрока и предмета в руке через Fabric Mixins (`CameraMixin`, `PlayerRendererMixin`, `ItemInHandRendererMixin`, `LocalPlayerMixin`, `SeamothTiltAccessor`);
  - обработка поведения мобов и защиты игрока (`SeamothAggroHandler`, `MobTargetMixin`, `PlayerFireImmunityMixin`).
- **Локализация:** поддержка английского (`en_us`), русского (`ru_ru`) и дореволюционного русского (`rpr`) языков.
- **CI/CD:** автоматическая сборка проекта настроена через GitHub Actions (`.github/workflows/build.yml`).

## Стек и инструменты

- **Язык:** Java *25*
- **Платформа:** Minecraft *26.1.2*, Fabric Loader, Fabric API
- **Инструменты:** Gradle (Gradle Wrapper), SpongePowered Mixin, GitHub Actions

## Структура проекта

Проект разделён на общую/серверную (`src/main`) и клиентскую (`src/client`) части:

```text
.
├── .github/workflows/build.yml         # CI-пайплайн сборки через Gradle
├── build.gradle / gradle.properties    # Конфигурация сборки и зависимостей
├── src/
│   ├── main/
│   │   ├── java/com/seamoth/
│   │   │   ├── Seamoth.java            # Основная точка входа мода
│   │   │   ├── entity/                 # Логика сущности SeamothEntity
│   │   │   ├── event/                  # Обработчики агра мобов и взаимодействий
│   │   │   ├── item/                   # Предметы, батареи, модули, вкладки и теги
│   │   │   ├── menu/                   # Серверная логика контейнера/GUI
│   │   │   ├── network/                # Сетевые пакеты (Input, Sync, OpenInventory)
│   │   │   └── mixin/                  # Общие/серверные миксины
│   │   └── resources/
│   │       ├── fabric.mod.json         # Метаданные Fabric-мода
│   │       ├── assets/seamoth/         # Текстуры, модели предметов, локализация (en/ru/rpr)
│   │       └── data/seamoth/tags/item/ # Датапак-теги (batteries, powercells, modules)
│   └── client/
│       ├── java/com/seamoth/client/
│       │   ├── SeamothClient.java      # Клиентская точка входа и обработчик
│       │   ├── SeamothKeybindings.java # Пользовательские бинды клавиш
│       │   ├── render/                 # 3D-модель, рендерер и RenderState
│       │   ├── screen/                 # Клиентский экран GUI (SeamothScreen)
│       │   ├── duck/                   # Duck-интерфейс наклона (SeamothTiltAccessor)
│       │   └── mixin/                  # Клиентские миксины камеры и рендера
│       └── resources/
│           └── seamoth.client.mixins.json
└── LICENSE
```

## Сборка и запуск из исходников

### Требования

- JDK *25*
- Git

### Команды сборки

```bash
git clone https://github.com/MmichailM/ModMaenkraftSeamoth.git
cd ModMaenkraftSeamoth

# Сборка .jar файла (Linux / macOS)
./gradlew build

# Сборка .jar файла (Windows)
gradlew.bat build
```

Готовый `.jar` файл появится в директории `build/libs/`.

### Запуск тестового клиента

```bash
./gradlew runClient
```

## Установка в Minecraft

1. Установи **Fabric Loader** и **Fabric API** под соответствующую версию Minecraft.
2. Помести собранный `.jar` файл из `build/libs/` (или из раздела **Releases**) в папку `mods/`.

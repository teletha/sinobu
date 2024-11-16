# Changelog

## [4.5.1](https://github.com/teletha/sinobu/compare/v4.5.0...v4.5.1) (2024-11-16)


### Bug Fixes

* Model#collectParameters is failed on parameterized type variable ([1357525](https://github.com/teletha/sinobu/commit/135752583d2bd5f5bbb4fd5d35e3afe0fd730805))

## [4.5.0](https://github.com/teletha/sinobu/compare/v4.4.1...v4.5.0) (2024-11-13)


### Features

* add XML#child(name, sub) ([f834aef](https://github.com/teletha/sinobu/commit/f834aefc5ce6cbf345373ed2423a1964ccb00651))

## [4.4.1](https://github.com/teletha/sinobu/compare/v4.4.0...v4.4.1) (2024-11-12)


### Bug Fixes

* more pretty xml format ([75b2ec3](https://github.com/teletha/sinobu/commit/75b2ec30d5d2e345cdb7bbee27d89c2c9deb6ed3))

## [4.4.0](https://github.com/teletha/sinobu/compare/v4.3.0...v4.4.0) (2024-11-11)


### Features

* add I#vouch ([c19c7b9](https://github.com/teletha/sinobu/commit/c19c7b9723ae3f798fff9da814e76cc59e972748))
* drop Signal#buffer(long, TimeUnit, ScheduledExecutorService...) ([4ba2b25](https://github.com/teletha/sinobu/commit/4ba2b2538338ee912ab5dcae5c51b855a4fb060b))
* drop Signal#buffer(Signal, Supplier, BiConsumer) ([8bd9cbe](https://github.com/teletha/sinobu/commit/8bd9cbe4a5ea6e353efbee18c94f4c998cc69aa4))
* drop Signal#buffer(WiseFunction) ([0738853](https://github.com/teletha/sinobu/commit/0738853176887939eeac7f8ca3b8a75b5bbe2d7b))
* drop Signal#is(V value) and #isNot(V value) ([b8e9dde](https://github.com/teletha/sinobu/commit/b8e9dde573e4eb6f63d21cc69d06c103d500ed6e))
* drop Signal#none(Predicate&lt;? super V&gt; condition) ([62b0e7e](https://github.com/teletha/sinobu/commit/62b0e7eca4fd5b5921f018185877fd2506c8c45c))
* drop Signal#pair and #pair(V init) ([a2bf620](https://github.com/teletha/sinobu/commit/a2bf6201e2eca5637434b9b2c38c4ad9a408475e))
* drop Signal#toAlternate ([44ded33](https://github.com/teletha/sinobu/commit/44ded33e7710bb05973ec227ef51795a70ab1ecf))
* drop Signal#wait(long, TimeUnit) ([d15e2ee](https://github.com/teletha/sinobu/commit/d15e2ee27200006173281d471d9c023ce1770eb8))
* drop Signale#to(Consumer&lt;? super V&gt; next, Runnable complete) ([f332b3f](https://github.com/teletha/sinobu/commit/f332b3fd7486a90a0f06f4697687c7a06871898a))


### Bug Fixes

* flaky test ([ae13f48](https://github.com/teletha/sinobu/commit/ae13f4815103bf6e9c6fb86d33b82b5e11268a44))

## [4.3.0](https://github.com/teletha/sinobu/compare/v4.2.0...v4.3.0) (2024-10-31)


### Features

* Model supports native-image ([0a56255](https://github.com/teletha/sinobu/commit/0a56255a09d591ffb9124e6582808160b295c188))
* new fallback Extensible system ([9df9fe5](https://github.com/teletha/sinobu/commit/9df9fe50c8e4f95084096318697ffa2bed1d5988))


### Bug Fixes

* log file writing error ([4b619ac](https://github.com/teletha/sinobu/commit/4b619ac8824b5b9616fcdd8ae92077e09b9e4b8f))
* remove deprecated api ([5db138e](https://github.com/teletha/sinobu/commit/5db138eeb046a0eecd07735ec9198df8ea033adc))

## [4.2.0](https://github.com/teletha/sinobu/compare/v4.1.0...v4.2.0) (2024-10-23)


### Features

* add I#schedule(cron, zoneId) ([3e57f17](https://github.com/teletha/sinobu/commit/3e57f17bdb81887e4cc540f83330eb96074b671a))
* change time provider from LognSupplier to Variable&lt;Long&gt; ([3e57f17](https://github.com/teletha/sinobu/commit/3e57f17bdb81887e4cc540f83330eb96074b671a))
* drop Signal#throttle with time provider ([3e57f17](https://github.com/teletha/sinobu/commit/3e57f17bdb81887e4cc540f83330eb96074b671a))

## [4.1.0](https://github.com/teletha/sinobu/compare/v4.0.0...v4.1.0) (2024-10-22)


### Features

* fast log writer ([17320ce](https://github.com/teletha/sinobu/commit/17320ce9cfe92d1a85201e4e5fcc0a63060b7ace))
* log supports unicode ([34b59a0](https://github.com/teletha/sinobu/commit/34b59a0f82b51af9c90dfbcbdeb774817d4c0b52))
* Scheduler#scheduleAt can specify the time zone ([c89d461](https://github.com/teletha/sinobu/commit/c89d46167ee7073efd79c4f870085e55e3a805fb))
* Support extra logger. ([e0bf1a1](https://github.com/teletha/sinobu/commit/e0bf1a16920c380dc3254f272252332d80871b61))

## [4.0.0](https://github.com/teletha/sinobu/compare/v3.14.1...v4.0.0) (2024-10-12)


### ⚠ BREAKING CHANGES

* update ci
* update java version to 21
* provide virtual scheduler with cron

### Features

* (cron) correct order ([2f3157d](https://github.com/teletha/sinobu/commit/2f3157d9ead18d2e59be50544416b913388d036d))
* (cron) correct order on various list ([7093f64](https://github.com/teletha/sinobu/commit/7093f64572eb53998ab6d838699c9a2166de56cc))
* (cron) support random keyword ([9d67ffd](https://github.com/teletha/sinobu/commit/9d67ffd9f5bdeb604132c8ed58ff606bb24eb96a))
* (cron) support weekday keyword ([c7c41cf](https://github.com/teletha/sinobu/commit/c7c41cfddd7926cd14f38af6836f938a5392a225))
* expose I#Jobs as global scheduler ([fd219b1](https://github.com/teletha/sinobu/commit/fd219b11d17119a5fc6355326f9734c4c6b52130))
* integrate cron with type ([78c4321](https://github.com/teletha/sinobu/commit/78c432117be9eb9707385934bfe231f17b719a66))
* provide virtual scheduler with cron ([e311eda](https://github.com/teletha/sinobu/commit/e311eda49c1f7e0c9d81079fac8a710c38a27a7e))
* support the limit of execution size ([8771ae5](https://github.com/teletha/sinobu/commit/8771ae5a4040536fc378a42c172383a1f93c0d46))
* update ci ([ffff772](https://github.com/teletha/sinobu/commit/ffff7728740503aa21d2c36d426c2c2bd9e17b17))
* update java version to 21 ([471d822](https://github.com/teletha/sinobu/commit/471d822162da105aa7ae718a5c35d898867bf43f))
* use virtual scheduler in default ([33e0fb1](https://github.com/teletha/sinobu/commit/33e0fb1fc00d892791e9f040e49b46daa1365045))


### Bug Fixes

* use non-static clean room ([a79feed](https://github.com/teletha/sinobu/commit/a79feed6aea98ed424fdfb8064577f6ec2cfa3d3))
* use non-static clean room in LogTest ([26b565f](https://github.com/teletha/sinobu/commit/26b565f7b6a5377684c7e54c64c8e40e4a25d194))
* use non-static clean room on no-virtual room ([2147c95](https://github.com/teletha/sinobu/commit/2147c95c7d0bc66244ceadd5883ee0339ea58cf7))
* use TestableScheduler instead of Chronos ([3bbec5b](https://github.com/teletha/sinobu/commit/3bbec5bca526fc5c8e4be5aa8a6b9ca50483e077))

## [3.14.1](https://github.com/teletha/sinobu/compare/v3.14.0...v3.14.1) (2024-09-27)


### Bug Fixes

* block section should not delete the extra header whitespace ([a9c61dd](https://github.com/teletha/sinobu/commit/a9c61dd0da1a53a2d27b7643079279cf9e0086f0))
* reduce code size ([8da9deb](https://github.com/teletha/sinobu/commit/8da9deb4b9499f897c9972ac368d6a28be9ecbc8))

## [3.14.0](https://github.com/teletha/sinobu/compare/v3.13.0...v3.14.0) (2024-09-13)


### Features

* add Signal#buffer(keySelector) ([04d8696](https://github.com/teletha/sinobu/commit/04d869678cfff767ad083bd13dbe9e74a06987e9))

## [3.13.0](https://github.com/teletha/sinobu/compare/v3.12.0...v3.13.0) (2024-08-21)


### Features

* change signature - Storable#locate returns Path instead of String ([d8755ca](https://github.com/teletha/sinobu/commit/d8755ca55c065acf690b0f32e8cdec2af14ec2ce))

## [3.12.0](https://github.com/teletha/sinobu/compare/v3.11.0...v3.12.0) (2024-08-17)


### Features

* suppress the sequencial same error in UncaughtExceptionHandler ([cd7191c](https://github.com/teletha/sinobu/commit/cd7191ca940316cd66c4ef8f135c0d7d9eabec4f))


### Bug Fixes

* make error notifier more simple ([ffe2a8c](https://github.com/teletha/sinobu/commit/ffe2a8c3537f83e3c5aa55c402c5e368d877bb30))
* synchronize Model#of ([271b095](https://github.com/teletha/sinobu/commit/271b0958bcef4bce5d066cf52ff28b21e0379a1e))

## [3.11.0](https://github.com/teletha/sinobu/compare/v3.10.1...v3.11.0) (2024-04-02)


### Features

* add Signal#mapError ([bb31643](https://github.com/teletha/sinobu/commit/bb3164370d5649b53748cbe7350ffff6044c4948))


### Bug Fixes

* reduce code size and memory size on runtime ([34fa56c](https://github.com/teletha/sinobu/commit/34fa56c37d3f4181f90b7e0c0d7e350c4a02a2be))
* revert null check ([97e0113](https://github.com/teletha/sinobu/commit/97e0113667879c46d0802fede683b75f065a2712))

## [3.10.1](https://github.com/teletha/sinobu/compare/v3.10.0...v3.10.1) (2024-03-01)


### Bug Fixes

* remove null check ([3d3ed9a](https://github.com/teletha/sinobu/commit/3d3ed9a31c76c495c56262420c4b7b71c18edb3a))
* update ci ([bfd27a1](https://github.com/teletha/sinobu/commit/bfd27a1f3967d0bef42a20ea43c3858d930ee4ea))

## [3.10.0](https://github.com/teletha/sinobu/compare/v3.9.3...v3.10.0) (2024-03-01)


### Features

* Signal#throttle and #debounce support lazy time resolver ([a487680](https://github.com/teletha/sinobu/commit/a487680dc7d31588266f2502874c21e049c98572))

## [3.9.3](https://github.com/teletha/sinobu/compare/v3.9.2...v3.9.3) (2024-02-20)


### Bug Fixes

* non-static inner class with Lifestyle dependency throws error ([a90c3b1](https://github.com/teletha/sinobu/commit/a90c3b1387117b662d6df58b95d3a247c975ae1c))

## [3.9.2](https://github.com/teletha/sinobu/compare/v3.9.1...v3.9.2) (2024-02-18)


### Bug Fixes

* Disposable forbids multiplex dispose call ([61d4343](https://github.com/teletha/sinobu/commit/61d4343de1b72ca89cc810baf9aca68fa0662b4d))

## [3.9.1](https://github.com/teletha/sinobu/compare/v3.9.0...v3.9.1) (2024-01-08)


### Bug Fixes

* update ci process ([f0b41af](https://github.com/teletha/sinobu/commit/f0b41af53b1c6335cad66105a4a01f6c19030663))

## [3.9.0](https://github.com/teletha/sinobu/compare/v3.8.0...v3.9.0) (2024-01-08)


### Features

* add I#prototype(type, injector) ([b854d64](https://github.com/teletha/sinobu/commit/b854d649d1926c851c948e5811accd90ec31ad45))


### Bug Fixes

* reduce code size ([d299c41](https://github.com/teletha/sinobu/commit/d299c415fae5e231dfb8f65b2f15dae2a1a749df))
* replace method reference by function ([7169c46](https://github.com/teletha/sinobu/commit/7169c469dc53a91dd7c009f8c0a49b484e4c67f6))
* typo ([c44ab24](https://github.com/teletha/sinobu/commit/c44ab24ab8ea560dcb956ea8f58f6585c530b132))
* typo ([000876f](https://github.com/teletha/sinobu/commit/000876f4038fd9ba35fa3f97110514bdfd7570d3))
* typo ([3c42136](https://github.com/teletha/sinobu/commit/3c42136e7177a5b3ef7a286465e592b3c787ec05))
* typo ([6315629](https://github.com/teletha/sinobu/commit/63156294d5abb6d97f6abbece2781d26627b913a))
* update license ([59856e6](https://github.com/teletha/sinobu/commit/59856e6b8c54f54ce7eb1a1095b171de15a3a11f))

## [3.8.0](https://github.com/teletha/sinobu/compare/v3.7.0...v3.8.0) (2023-12-09)


### Features

* Add Variable#flip and #flatFlip. ([bfedf8e](https://github.com/teletha/sinobu/commit/bfedf8ecefb24a82dc21c3a5fcd2f6700c765e29))


### Bug Fixes

* Don't interrupt the executing task. ([e0ae8c4](https://github.com/teletha/sinobu/commit/e0ae8c4fc6e3ca45e983987d800a6e0f332c44de))
* JSON parser use the symbol table for key only. ([c4d83e2](https://github.com/teletha/sinobu/commit/c4d83e2f54c7a9f0452ca2d5f670a9cfea48a993))
* Locale encoder should use language tag only. ([8aa158f](https://github.com/teletha/sinobu/commit/8aa158f79d7dac5df3413b05ca5d1ee8dc47c6f2))
* Model is not defined when parameter variables are detected. ([13a430c](https://github.com/teletha/sinobu/commit/13a430c632a32aee7109b90f3b8e4618cd8e93b2))
* Signal#on ensures that the complete event is invoked at last. ([8416522](https://github.com/teletha/sinobu/commit/8416522cf46a666391a7a640ab5cb7551ba5c5f7))

## [3.7.0](https://github.com/teletha/sinobu/compare/v3.6.2...v3.7.0) (2023-08-26)


### Features

* Remove model package. ([3b4dca2](https://github.com/teletha/sinobu/commit/3b4dca2c9fb746b43bd1fbd559fdbe7c892822f9))
* Remove Parameterized. ([498d391](https://github.com/teletha/sinobu/commit/498d391f1a1f08ffa12035b2d9d51849819a0d8e))
* Support nested parameterized type. ([0fcc443](https://github.com/teletha/sinobu/commit/0fcc4434e2bd4417ba4e412eed6e3bc6623d0a08))
* Support the partial typed map. ([b69ef97](https://github.com/teletha/sinobu/commit/b69ef97e1a451f58b9e26843fe81ebb7e56922cc))


### Bug Fixes

* Don't use exponential notation on BigDecimal. ([f944cf2](https://github.com/teletha/sinobu/commit/f944cf2cc9718959c0ac794ba9753a571e26959e))
* logback benckmark is broken. ([5263d1d](https://github.com/teletha/sinobu/commit/5263d1dbd64b8cd4dc7068caa39402e21d09a170))

## [3.6.2](https://github.com/teletha/sinobu/compare/v3.6.1...v3.6.2) (2023-06-24)


### Bug Fixes

* Signal#scan(Collector) must make new container each accumulation. ([3cd0aae](https://github.com/teletha/sinobu/commit/3cd0aae3f174b94ad30a46dad0524e1291b5bbc9))

## [3.6.1](https://github.com/teletha/sinobu/compare/v3.6.0...v3.6.1) (2023-06-16)


### Bug Fixes

* release please ([f7d7e2b](https://github.com/teletha/sinobu/commit/f7d7e2bde99f8e99a1f8b61470bc3c4bfa14bc46))

## [3.6.0](https://github.com/teletha/sinobu/compare/v3.5.0...v3.6.0) (2023-06-16)


### Features

* I#find and #findAs support enum extension. ([a4d4e5c](https://github.com/teletha/sinobu/commit/a4d4e5c87f822c64eeec943ac7be91f2c26b9f34))

## [3.5.0](https://github.com/teletha/sinobu/compare/v3.4.0...v3.5.0) (2023-06-06)


### Features

* Managed#name can declare the user custom name. ([e0ddec1](https://github.com/teletha/sinobu/commit/e0ddec1848304d421916098d91e595ac33baa0ad))

## [3.4.0](https://github.com/teletha/sinobu/compare/v3.3.0...v3.4.0) (2023-06-04)


### Features

* Model based JSON encoder and decoder. ([2b8c513](https://github.com/teletha/sinobu/commit/2b8c513c69dcd83289f332da293ec6e309b9d6b6))

## [3.3.0](https://github.com/teletha/sinobu/compare/v3.2.0...v3.3.0) (2023-05-18)


### Features

* I#translate can dispose automatic translation. ([69a864b](https://github.com/teletha/sinobu/commit/69a864b17d7d9fff76d2bfb8dd12a685a6ea921c))

## [3.2.0](https://github.com/teletha/sinobu/compare/v3.1.0...v3.2.0) (2023-03-02)


### Features

* Add Signale#skipIf and #takeIf. ([3dc49ca](https://github.com/teletha/sinobu/commit/3dc49caa474aac7b2fbe23fc649592333955d2eb))

## [3.1.0](https://github.com/teletha/sinobu/compare/v3.0.1...v3.1.0) (2023-01-04)


### Features

* Add Signale#waitForTerminate(rethrow) ([81a7f28](https://github.com/teletha/sinobu/commit/81a7f284dc1d7c766a9f7f77afb0dfaf52323c12))
* Signal#waitForTerminate always rethrow error ([360983d](https://github.com/teletha/sinobu/commit/360983d81b61c33784d2204f2d280ab2f68360fe))

## [3.0.1](https://github.com/teletha/sinobu/compare/v3.0.0...v3.0.1) (2023-01-01)


### Bug Fixes

* non-accessible model (i.e. java.util.Date) will throw exception ([e36d74c](https://github.com/teletha/sinobu/commit/e36d74c6968c12f51fbd4068d415ebd5ec07b601))
* Path codec supports non-windows environment ([df59d3a](https://github.com/teletha/sinobu/commit/df59d3a2773117e7c0a99fc2ef4c660be5e318cc))

## [3.0.0](https://github.com/teletha/sinobu/compare/v2.23.0...v3.0.0) (2022-12-20)


### ⚠ BREAKING CHANGES

* Drop Tree and TreeNode.

### Features

* Drop Tree and TreeNode. ([bf08e8d](https://github.com/teletha/sinobu/commit/bf08e8d13b60c0dc25f900aa6a0486aaef881cb5))

## [2.23.0](https://github.com/teletha/sinobu/compare/v2.22.2...v2.23.0) (2022-12-20)


### Features

* export structure builder as lycoris ([bdb674b](https://github.com/teletha/sinobu/commit/bdb674b2baf70dad2bc5ab884f3e161fd76ce2c0))

## [2.22.2](https://github.com/teletha/sinobu/compare/v2.22.1...v2.22.2) (2022-12-19)


### Bug Fixes

* I#xml with blank string must throw exception ([a6053b7](https://github.com/teletha/sinobu/commit/a6053b7034466c637be4522a1303829e459b3ef1))

## [2.22.1](https://github.com/teletha/sinobu/compare/v2.22.0...v2.22.1) (2022-12-18)


### Bug Fixes

* I#xml can parse text with heading whitespaces. ([bdebd85](https://github.com/teletha/sinobu/commit/bdebd85c1fea44e2706a73590601669916024e3d))

## [2.22.0](https://github.com/teletha/sinobu/compare/v2.21.0...v2.22.0) (2022-12-17)


### Features

* Expose mustache method with configurable delimiters. ([c556d52](https://github.com/teletha/sinobu/commit/c556d52857ee61121e9e4c32355649eeea0b3079))


### Bug Fixes

* XML#parent will occur UnsupportedOperationException. ([6a438df](https://github.com/teletha/sinobu/commit/6a438df307263c88fa9ef166c4598e6097a2cb44))

## [2.21.0](https://github.com/teletha/sinobu/compare/v2.20.0...v2.21.0) (2022-11-29)


### Features

* JSON parser can detect more granular syntax errors. ([7fdbc0b](https://github.com/teletha/sinobu/commit/7fdbc0b7590fc79d1cc2834aac8eb22e2c7d12bb))


### Bug Fixes

* I#express can't resolve empty path. ([2f33072](https://github.com/teletha/sinobu/commit/2f330720c79655b4c5c6866ce342fd42ac1d5bd7))
* JSON reduces memory usage ([0ff299f](https://github.com/teletha/sinobu/commit/0ff299f1426cb786300103790db615ec02a9cbc2))
* Model#set is simplified. ([c1a1fd3](https://github.com/teletha/sinobu/commit/c1a1fd38ccf00ec1f3a3eb4680b6388eb33ef79c))
* Optimize parsing for small JSON ([18ba233](https://github.com/teletha/sinobu/commit/18ba2339fe713ce2ffd43bfc08f2d1dc73b48f9a))
* Property access is more faster. ([484a557](https://github.com/teletha/sinobu/commit/484a5570439cf65448417227d0a006becb1bcf11))
* reduce code size ([d1ed126](https://github.com/teletha/sinobu/commit/d1ed126fe4a3a2465a54dedf7784f94d38a0ae67))
* remove duplicated code ([73eb4fe](https://github.com/teletha/sinobu/commit/73eb4fe8561adf707f0ac0d2c5cff2c71e2bec3f))
* remove restriction for non-attribute final field ([e949af4](https://github.com/teletha/sinobu/commit/e949af40d21eb8a9a1aae457d7431f5f18dd17fd))
* remove unused code ([fa4acd3](https://github.com/teletha/sinobu/commit/fa4acd3ff6246984463ac201ca955eddfed7e531))
* Simplify MapModel. ([4f9167e](https://github.com/teletha/sinobu/commit/4f9167e151f83b16d1ed4b15fab4ffd6d814e9de))
* Speed up I#make. ([149e88c](https://github.com/teletha/sinobu/commit/149e88ca84b64449df42e52a85a12ba7eb4d45a4))
* Speed up JSON parsing. ([068f1e5](https://github.com/teletha/sinobu/commit/068f1e5710a2bf4b1bfb6153eeb96b25a66f215d))
* speed up json(String) parsing ([97585f0](https://github.com/teletha/sinobu/commit/97585f0430541466c3c084ffd61657069cdc9591))
* speed up parsing boolean value ([187b2fe](https://github.com/teletha/sinobu/commit/187b2feb41788afb05c5ee603bf85fd1d0b47529))
* Update antibug. ([fd5cbaf](https://github.com/teletha/sinobu/commit/fd5cbaf58c23f0fa1ab7829dd205d612a159d9fa))

## [2.20.0](https://github.com/teletha/sinobu/compare/v2.19.0...v2.20.0) (2022-08-30)


### Features

* Revert Model#properties. ([9c7c31b](https://github.com/teletha/sinobu/commit/9c7c31b2d4d66151a5beecdd764b52950ff7eb1f))

## [2.19.0](https://github.com/teletha/sinobu/compare/v2.18.0...v2.19.0) (2022-08-30)


### Features

* Model holds its properties on TreeMap. ([bd12ca7](https://github.com/teletha/sinobu/commit/bd12ca711c0a7ec8227db94af657d4fa6e795608))
* Remove Model#name. ([b369376](https://github.com/teletha/sinobu/commit/b369376ccff4d67672e754214075ea19b8b509cc))
* Remove Model#properties ([391d05a](https://github.com/teletha/sinobu/commit/391d05af3c50683dc2f4fadbb4e30fa6e0a9ecf5))
* Speed up JSON parsing. ([6020a05](https://github.com/teletha/sinobu/commit/6020a05cb011ae8949dea72c73d2323b9b7a11c4))


### Bug Fixes

* benchmark ([18e672e](https://github.com/teletha/sinobu/commit/18e672e28e044be5fe956fbfc303c683fa908ca3))
* Optimize json parser. ([124a8a8](https://github.com/teletha/sinobu/commit/124a8a8a0ae5d5243e7e17d516e65a92a3397ba0))
* speed up json parsing ([d9423a8](https://github.com/teletha/sinobu/commit/d9423a866493c292efd4e1d978df77f66f11f81f))
* Speed up parsing JSON. ([f5b9b69](https://github.com/teletha/sinobu/commit/f5b9b696371d404193420a9d817bbdc4011e61d9))
* Use google translation API. ([b428238](https://github.com/teletha/sinobu/commit/b428238c4da87c66e5dbdee44364f325472252fb))

## [2.18.0](https://github.com/teletha/sinobu/compare/v2.17.0...v2.18.0) (2022-08-11)


### Features

* Model uses full-typed cache. ([a9f5cc3](https://github.com/teletha/sinobu/commit/a9f5cc3a61f6b897e75576bb239ea5b3b147491d))


### Bug Fixes

* Speed up I#express. ([c51f1a7](https://github.com/teletha/sinobu/commit/c51f1a7cb5e35c5def84f79f923d29b2cf9b175c))

## [2.17.0](https://www.github.com/teletha/sinobu/compare/v2.16.0...v2.17.0) (2022-08-08)


### Features

* Make I#express 60% faster . ([cccda4a](https://www.github.com/teletha/sinobu/commit/cccda4a3f29e8afca281eed1a7b657e76e14db50))
* Make I#express 90% faster by replacing full-scratched parser. ([7731d8d](https://www.github.com/teletha/sinobu/commit/7731d8dd23af10b498709714ce114c2e91a35299))


### Bug Fixes

* Make I#express more faster. ([dba2428](https://www.github.com/teletha/sinobu/commit/dba2428da41ee6de6208efe12cbddb1c9b749969))
* reduce code size ([6fec517](https://www.github.com/teletha/sinobu/commit/6fec517e3f76aaa3c083182eedaaa7edad95be1a))
* reduce code size ([7d5f38b](https://www.github.com/teletha/sinobu/commit/7d5f38b613d3a0c3ad5ab33e6b5d3759665128db))
* Upgrade the default file log level to INFO. ([7dfeaa7](https://www.github.com/teletha/sinobu/commit/7dfeaa76f121c378fd8d7f657bb16528422e9dc8))

## [2.16.0](https://www.github.com/teletha/sinobu/compare/v2.15.0...v2.16.0) (2022-06-29)


### Features

* Add WiseQuad consumer and function. ([ae155ea](https://www.github.com/teletha/sinobu/commit/ae155ea72bb035d57bf2998d848f560e205df255))


### Bug Fixes

* Delete the lock file on close. ([a499a2c](https://www.github.com/teletha/sinobu/commit/a499a2c1923d993ad7779f5c60f2a401ddbae32d))
* Increase symbol cache size. ([0bd568a](https://www.github.com/teletha/sinobu/commit/0bd568a80debd22a71c3d779227f2cd125ae3be5))
* JSON's reversed wildcard was broken. ([c4c485f](https://www.github.com/teletha/sinobu/commit/c4c485f54f68b9462fe3bf969e67a77a3db8b88e))
* JSON#find loops infinitely when attribute value is mixed. ([e74773e](https://www.github.com/teletha/sinobu/commit/e74773e7f8d3f5cfae1fada76de147414fe453e9))
* Reduce code size. ([0446e83](https://www.github.com/teletha/sinobu/commit/0446e834ba46b2880d311229cb0754fa8316804e))
* Reduce memory usage and footprint. ([a4246f8](https://www.github.com/teletha/sinobu/commit/a4246f888223788daf53fa65b375133173e40851))
* remove quad functions ([649ac7a](https://www.github.com/teletha/sinobu/commit/649ac7abe721402146fd03ae2fbe970aa5364461))
* Signal#pair emits values immediately. ([b9a34ae](https://www.github.com/teletha/sinobu/commit/b9a34ae7e5845d3e51215b0a7742ea1d61d102e8))
* Storable write data more safely. ([10b008f](https://www.github.com/teletha/sinobu/commit/10b008f90eb94ede68ef20f2a72445c62d4b83fd))
* XML builder uses the qualified element name. ([1693ef0](https://www.github.com/teletha/sinobu/commit/1693ef0f9d1ffe754eb3c50c7fde428442d21909))
* XML#child returns the compounded XML. ([7fc6f0a](https://www.github.com/teletha/sinobu/commit/7fc6f0aa5fe7d6a72bd5f029a5f30fbcdd1e7166))
* XPath which indicates parent node "../" is not supported. ([dd0978d](https://www.github.com/teletha/sinobu/commit/dd0978da9f4760b02067c2b834a3c5ca91357325))

## [2.15.0](https://www.github.com/teletha/sinobu/compare/v2.14.0...v2.15.0) (2022-02-20)


### Features

* Add Signal#debounce(time, unit, first, scheduler). ([b5a0854](https://www.github.com/teletha/sinobu/commit/b5a0854a8dd7de28486426219a7ae11de3fb924d))


### Bug Fixes

* Change default number of days to keep log from 30 days to 90 days. ([7a7eb77](https://www.github.com/teletha/sinobu/commit/7a7eb77e907967d9245f482638ca29657da260a9))
* Drop Signal#combineLatest(constnat). ([75932f5](https://www.github.com/teletha/sinobu/commit/75932f55bae1c6f818ee3ea29dc62e707e2f1f77))

## [2.14.0](https://www.github.com/teletha/sinobu/compare/v2.13.1...v2.14.0) (2022-02-10)


### Features

* Greatly improved the speed of HTML parsing. ([e35ab6e](https://www.github.com/teletha/sinobu/commit/e35ab6e2e25f2a806bc489a30c45f5c2be083adf))
* JSON parser enables symbol table. ([57daa85](https://www.github.com/teletha/sinobu/commit/57daa85cd18db1ec3b724a12195ce7aab6037753))


### Bug Fixes

* Improve performance of XML#child. ([6b3894b](https://www.github.com/teletha/sinobu/commit/6b3894b13d8a30a65f84ae34fbd020c2061f6035))
* JSON's symbol table is broken. ([b2b0bac](https://www.github.com/teletha/sinobu/commit/b2b0bacdde48ba3d7e313329fd09f28537ddb5e8))
* Reduce code size and enlarge the interned index. ([a335b8e](https://www.github.com/teletha/sinobu/commit/a335b8e9d23928da1bed2a08652d065c5dcfb81f))
* Reduce code size. ([01ef807](https://www.github.com/teletha/sinobu/commit/01ef807c24b0f51d2510e5e3cc14982711b06e34))

### [2.13.1](https://www.github.com/teletha/sinobu/compare/v2.13.0...v2.13.1) (2022-02-03)


### Bug Fixes

* Reduce code size by formatting. ([c90e29c](https://www.github.com/teletha/sinobu/commit/c90e29cca416696a2c5d2a31f386c4eb2513cfd6))

## [2.13.0](https://www.github.com/teletha/sinobu/compare/v2.12.0...v2.13.0) (2022-01-15)


### Features

* Expose the low-level logging method for other facade. ([ae9e790](https://www.github.com/teletha/sinobu/commit/ae9e790f987c3b0e2b0aedf974441a8a24f38836))
* JSON parser uses memory more effectively. ([db68e1a](https://www.github.com/teletha/sinobu/commit/db68e1a66082e2e8d329a552c67462f9c8a37c96))
* Provide initialization flag. ([192b553](https://www.github.com/teletha/sinobu/commit/192b553727509abea655f6c5c4e78931f42b2b9b))


### Bug Fixes

* Support java 11. ([53ff2f9](https://www.github.com/teletha/sinobu/commit/53ff2f9f8abec6808219c8effd2ab6973c892696))
* Update java to 17. ([6c8c948](https://www.github.com/teletha/sinobu/commit/6c8c9489aa3a5dc968a26941f807f1db978bdb61))

## [2.12.0](https://www.github.com/teletha/sinobu/compare/v2.11.0...v2.12.0) (2022-01-05)


### Features

* Single#joinAll and #joinAny accepts ExecutorService. ([dee701f](https://www.github.com/teletha/sinobu/commit/dee701f25ebf597bd00fdc8c88c3daddecb8a61f))


### Bug Fixes

* CSS selector accepts element name with hyphen. ([6806c86](https://www.github.com/teletha/sinobu/commit/6806c861b1b3dbcc772c16d6377657e84a028104))
* CSS selector accepts unescaped hyphen. ([6806c86](https://www.github.com/teletha/sinobu/commit/6806c861b1b3dbcc772c16d6377657e84a028104))
* Disable external resource access on XML parser. ([4464fe4](https://www.github.com/teletha/sinobu/commit/4464fe489235774dc9d1d9b12ef0339baa740852))

## [2.11.0](https://www.github.com/teletha/sinobu/compare/v2.10.0...v2.11.0) (2021-12-23)


### Features

* I#express can access the parent context in section. ([ec19054](https://www.github.com/teletha/sinobu/commit/ec19054fe89e26c3d4813b52c1512c875f0617f2))
* I#express can change the delimiter at runtime. ([26c780a](https://www.github.com/teletha/sinobu/commit/26c780aad39ff43c97f8f6bcf0f7f5c7d98d642c))


### Bug Fixes

* Change delimiter must affect globally. ([6d747f4](https://www.github.com/teletha/sinobu/commit/6d747f49eddae581ea4b3c1117b6e7ec4477058e))
* I#express can't resolve null property well. ([285b59c](https://www.github.com/teletha/sinobu/commit/285b59c5c5901af6915d0976aa76e851f0ba14f0))
* I#express outputs invalid empty line. ([3f6d680](https://www.github.com/teletha/sinobu/commit/3f6d680777b3523d1964d92648f7d4440b433c21))
* In I#express, section with long delimiter is broken. ([1358ad1](https://www.github.com/teletha/sinobu/commit/1358ad19aba73c63ec9f44b8d8cbfad117afa1be))

## [2.10.0](https://www.github.com/teletha/sinobu/compare/v2.9.4...v2.10.0) (2021-11-14)


### Features

* Add JSON#asMap(Class<M>). ([00995c2](https://www.github.com/teletha/sinobu/commit/00995c2b4511fa232e64dfa5e1f6c10a97ff7197))

## [2.9.0](https://www.github.com/Teletha/sinobu/compare/v2.8.0...v2.9.0) (2021-11-09)


### Features

* Remove debug info from class files. ([9aba01c](https://www.github.com/Teletha/sinobu/commit/9aba01cc00d54f4e440d64500c518c9b83ea3beb))


### Bug Fixes

* I#json(String) and #xml(String) accepts URL text. ([3a9478f](https://www.github.com/Teletha/sinobu/commit/3a9478f549090801f754fb2b7a3d88dd1691f19d))

## [2.8.0](https://www.github.com/Teletha/sinobu/compare/v2.7.0...v2.8.0) (2021-10-21)


### Features

* Add I#wiseF(constant) and #wiseS(constant). ([075560c](https://www.github.com/Teletha/sinobu/commit/075560c3766f2eada35c43257fd6f5b4f958e32b))
* Drop Enumeration support for Signal. ([d05017c](https://www.github.com/Teletha/sinobu/commit/d05017c9fa07c936146c95e06386e1b2bac244ee))
* Drop I#wiseBC and #wiseBF. ([fa42ffb](https://www.github.com/Teletha/sinobu/commit/fa42ffb4eb8387252e84019a061d5d14c428283f))
* Drop I#wiseTC and #wiseTF ([1716c6d](https://www.github.com/Teletha/sinobu/commit/1716c6d74b3cd8fb661410efffbdae36036441e3))
* Drop Signal#buffer(Signal boundary, Supplier collection). ([9306315](https://www.github.com/Teletha/sinobu/commit/9306315c627d17fa64f96a36463553366bb62bf1))
* Drop Signal#contains, use #any instead. ([123e9e8](https://www.github.com/Teletha/sinobu/commit/123e9e8593824a88b24c4d44e76ef0afc5e176be))
* Drop Signal#diff(WiseFunction<V, K> keySelector). ([05c5d39](https://www.github.com/Teletha/sinobu/commit/05c5d39c7b0d76bc63e79aece286446e40c9e60a))
* Drop Signal#errorResume and add Signal#stopError. ([c458c06](https://www.github.com/Teletha/sinobu/commit/c458c06a7f4ccd9f937d5c2b8b2d3cc5683c66fa))
* Rename from Signal#combineLatestMap to #keyMap. ([91d42fd](https://www.github.com/Teletha/sinobu/commit/91d42fd73abaed41fe67fcc63311f06b88ece640))


### Bug Fixes

* Change signatures for Signal#map and #flatMap with context. ([74ac5c5](https://www.github.com/Teletha/sinobu/commit/74ac5c5750e05ff1dab0195c58008935fbd67382))
* Narrow#bindLazily and #bindLastLazily reject null parameter. ([65a46ff](https://www.github.com/Teletha/sinobu/commit/65a46ff46c8a66f7a72fda48595edecd0c375a07))
* Signal#as accepts null type. ([9677713](https://www.github.com/Teletha/sinobu/commit/9677713a62fa8b946adc0934c97459175a09b83e))

## [2.7.0](https://www.github.com/Teletha/sinobu/compare/v2.6.0...v2.7.0) (2021-10-14)


### Features

* Add Signal#pair. ([c115337](https://www.github.com/Teletha/sinobu/commit/c115337da99899891e3bd6e61cf72a41cb2566d3))
* Drop all Signal#maps methods. ([4200ccb](https://www.github.com/Teletha/sinobu/commit/4200ccbbf1d5479c78fa0ee423bbcff710f62270))
* Drop Signal#delay(long count), use #buffer(long, long) instead. ([03d8d4e](https://www.github.com/Teletha/sinobu/commit/03d8d4eb8c2a6ec2f6a70993d12cc300cb5c7048))
* Drop Signal#recover and #retry with configurable parameters. ([d811713](https://www.github.com/Teletha/sinobu/commit/d811713db080f653477dac6c7486820cdf480057))
* Drop Signal#repeat with configurable parameters. ([7053181](https://www.github.com/Teletha/sinobu/commit/7053181d709792dde66b1fa19ab1c66a338bf28d))
* Drop Signal#skipError without parameters. ([ada0043](https://www.github.com/Teletha/sinobu/commit/ada0043c695f9682d9cd3ee85ee904e5f3c8a0c2))
* Drop Signal#take and #skip by duration, use #xxxUntil instead. ([bbf51d5](https://www.github.com/Teletha/sinobu/commit/bbf51d59fa52f445ae5279042b7fbc7c326d6c11))
* Drop Signal#take(Collection) and #skip(Collection). ([ab7bc80](https://www.github.com/Teletha/sinobu/commit/ab7bc80b1238054861065016008e9f5ec98f723c))
* Drop Signal#takeUntil(Object) and #skipUntil(Object). ([20eab42](https://www.github.com/Teletha/sinobu/commit/20eab42c12e9205db41a51bb81361124d07fe374))
* Drop Signale#recover and #retry with error type. ([7f739ed](https://www.github.com/Teletha/sinobu/commit/7f739ed4d91d145d5dc953cdcb7e8f109d299b40))

## [2.6.0](https://www.github.com/Teletha/sinobu/compare/v2.5.0...v2.6.0) (2021-10-11)


### Features

* Drop I#accept and #reject. Use I.Accept and I.Reject instead. ([aead9a5](https://www.github.com/Teletha/sinobu/commit/aead9a5cccab5c3bf03ecfbf0f122424aa93030f))
* Drop I#join, use String#join instead. ([c14895f](https://www.github.com/Teletha/sinobu/commit/c14895fa4228767dbba0e7b794bdaa694c1efa8f))
* Drop Model#get(Object, String). ([7e42cb5](https://www.github.com/Teletha/sinobu/commit/7e42cb54e8d2ee413b5e1b2643b0222c9eb38472))
* Drop Signal#delay(Supplier<Duration>). ([483d746](https://www.github.com/Teletha/sinobu/commit/483d746fb625543fb03ccc8f38e85dbfe5343d93))
* Drop Signal#effectOnComplete(List), use #buffer() instead. ([ece3703](https://www.github.com/Teletha/sinobu/commit/ece37036d5b2c1281856a4ecc939f756023a537a))
* Drop Signal#index without starting index number. ([391d47f](https://www.github.com/Teletha/sinobu/commit/391d47fa190bc2ddd61479d30ce88374ac04b851))
* Drop Signal#scanWith, use #scan(Supplier, WiseBiFunction). ([7d7b16b](https://www.github.com/Teletha/sinobu/commit/7d7b16b3f21150ff8ae5d7b32552fbc11b5c08d2))
* Drop Signal#single and #size(int). ([ba57f4e](https://www.github.com/Teletha/sinobu/commit/ba57f4ecfc43e4a6820303f034ed52e219c31b77))
* Drop Signal#skipAll. ([0317e32](https://www.github.com/Teletha/sinobu/commit/0317e327d8c0a4895be430f7e42031d1dad7a600))
* Drop Signal#to(Class<? extend Collection>). ([94a934b](https://www.github.com/Teletha/sinobu/commit/94a934be12fbb08575daab6cb84c63b46786cefb))
* Drop Signal#toBinary, use Signal#toggle instead. ([5f9ec96](https://www.github.com/Teletha/sinobu/commit/5f9ec96123360f7c0763dc936b3e035018bc9021))
* Drop Signale#toggle without initial boolean value. ([c5700e3](https://www.github.com/Teletha/sinobu/commit/c5700e388a00ebef972c536fcfa1f2d17f8c596a))
* Drop Signale#toggle(boolean). ([07e1666](https://www.github.com/Teletha/sinobu/commit/07e1666d6c079537307a30c70cc95e304c1c8fc5))
* Drop support I#bundle for Iterable. ([0a48b8c](https://www.github.com/Teletha/sinobu/commit/0a48b8c810b7eff0f395283b66ddf09b12b17c44))
* Drop support I#schedule without ScheduledExecutorService. ([a8c7729](https://www.github.com/Teletha/sinobu/commit/a8c77292dc3e639c513a0927d2e3a9548e7b4d26))
* Drop time related Signal methods without ScheduledExecutorService. ([211f421](https://www.github.com/Teletha/sinobu/commit/211f42154f5c57aaa255088f94649355edc3b633))
* Provide generic aware I#accept and #reject. ([5521932](https://www.github.com/Teletha/sinobu/commit/5521932be797412c2ca28e1afcadcb53fa944447))


### Bug Fixes

* Signal#skip(Signal) is broken. ([d8bc6af](https://www.github.com/Teletha/sinobu/commit/d8bc6afcb3b730309da93f043c35e8730e696a7e))

## [2.5.0](https://www.github.com/Teletha/sinobu/compare/v2.4.1...v2.5.0) (2021-10-09)


### Features

* Constructor to be used preferentially during DI can be specified ([eddec34](https://www.github.com/Teletha/sinobu/commit/eddec34d8ef85ab104251eda4811bb5804bd21ae))
* Support partial constructor injection by Inject annotation. ([ac6f609](https://www.github.com/Teletha/sinobu/commit/ac6f6092a8422666cc035fe36c4a36493522c78d))


### Bug Fixes

* Make console logging more faster. ([bddd3d9](https://www.github.com/Teletha/sinobu/commit/bddd3d967a34d5a2d13544b3d6b40d3a5c2dea90))
* Singleton accepts the upper-bounded implementation class. ([a66b1f3](https://www.github.com/Teletha/sinobu/commit/a66b1f332d501d43543371c120958ffc2257f2c7))

### [2.4.1](https://www.github.com/Teletha/sinobu/compare/v2.4.0...v2.4.1) (2021-10-07)


### Bug Fixes

* Multiple loggers lost the message to console. ([5cb0163](https://www.github.com/Teletha/sinobu/commit/5cb01630e0effcbd350e8cf59e70888f54911847))

## [2.4.0](https://www.github.com/Teletha/sinobu/compare/v2.3.1...v2.4.0) (2021-10-06)


### Features

* (Re)Provide logging methods. ([0861c16](https://www.github.com/Teletha/sinobu/commit/0861c16b663168f51c581fa0dddd659e36cfe4a5))
* Add 'LogAppend' configuration. ([52d2bb5](https://www.github.com/Teletha/sinobu/commit/52d2bb588a5b4aac461fd194794533ec5676aaf4))
* Add logging utilities (I#trace #debug #info #warn #error) ([b4edb16](https://www.github.com/Teletha/sinobu/commit/b4edb1658457d453a791efad2a55ead6619f1b70))
* Add various configurable logging parameters. ([a8dd676](https://www.github.com/Teletha/sinobu/commit/a8dd67635a10f0671c1363a148f5f9bc4e2feb78))
* File logger can configure the file rotation size using ([67778c1](https://www.github.com/Teletha/sinobu/commit/67778c109d9d637aa4116698896a259b6cbb7de6))
* High speed logging utility. ([360bf55](https://www.github.com/Teletha/sinobu/commit/360bf55d67939742eebb059fc89ac371f6e6c430))
* I#env is strongly typed by the default value. ([e4dc29c](https://www.github.com/Teletha/sinobu/commit/e4dc29cafa84cc9d5a141238c3606675e0b76c80))
* Log can flash smartly now. ([75daa68](https://www.github.com/Teletha/sinobu/commit/75daa6844b42ab7c6fb76a2230e686a9b46cbdc8))
* Provide garbage-free logging. ([6fb326e](https://www.github.com/Teletha/sinobu/commit/6fb326e5a9bf55ea50e110ab7399646ea1d603b4))
* User can configure the log directory for each loggers. ([214ea60](https://www.github.com/Teletha/sinobu/commit/214ea60ed6880ec054c5aa7fa2c32c059de56d75))
* User can define the extra log handler. ([836fb7d](https://www.github.com/Teletha/sinobu/commit/836fb7dbc9afe32fd97b43aedbcfaabbfae18c92))


### Bug Fixes

* Clear logger buffer. ([118a773](https://www.github.com/Teletha/sinobu/commit/118a7739cb26776f0216e2dde063f324a4854d20))
* Enhance logging utility. ([8171859](https://www.github.com/Teletha/sinobu/commit/817185967604803f03fc4dd50ad2f5a92cd0437d))
* Failed to inter type. ([6e341f3](https://www.github.com/Teletha/sinobu/commit/6e341f340e6fcc50559bbfc289f3eb87b3803665))
* File logger can delete old sparse files. ([2af4e86](https://www.github.com/Teletha/sinobu/commit/2af4e864e8da1e05c9d74dc10742d35afb639193))
* File logger can flush automatically. ([0ff6ea3](https://www.github.com/Teletha/sinobu/commit/0ff6ea33b3614c5a6ca90775a7abb3845add2bfd))
* Log files are generated only when they are needed. ([3b9fa10](https://www.github.com/Teletha/sinobu/commit/3b9fa105845eebb112ce5738ed7ffdec96005385))
* Logging is not flushed immediately. ([c864092](https://www.github.com/Teletha/sinobu/commit/c864092d5131526c2c93f2c14d885e7e2418c9a9))
* Make async logging more fast. ([100c34c](https://www.github.com/Teletha/sinobu/commit/100c34c8172077827ef59e736b5d9633f3221102))
* Make logging more faster. ([c22d762](https://www.github.com/Teletha/sinobu/commit/c22d76215ccf4a6969750f4cf7b61a8f076d4a98))
* Make logging more faster. ([92adae8](https://www.github.com/Teletha/sinobu/commit/92adae852e0932c5488d8c0bdb5c4dd5198fe46a))
* Make logging more faster. ([882073f](https://www.github.com/Teletha/sinobu/commit/882073fa22da7f772d37f8fa912bab67e71d6982))
* Reduce creating instance on logging. ([56263f0](https://www.github.com/Teletha/sinobu/commit/56263f059c0a9b75ebee8a99a5a8f17a213ecf7b))
* Reduce memory usage on IO task. ([d284915](https://www.github.com/Teletha/sinobu/commit/d28491519bf31de57fee87b438fe3d05004ebe8f))
* Sets the length of the log level display name to a fixed width (5). ([96403d1](https://www.github.com/Teletha/sinobu/commit/96403d1d507c68e048efde01ba29c23feed1e6bc))
* Update pom. ([eaeb35d](https://www.github.com/Teletha/sinobu/commit/eaeb35ddcc0ac8493f9912803d4e1d3fa27320f9))

### [2.3.1](https://www.github.com/Teletha/sinobu/compare/v2.3.0...v2.3.1) (2021-09-07)


### Bug Fixes

* CSS selector can accept the escaped class name. ([a418317](https://www.github.com/Teletha/sinobu/commit/a418317ace1525545daad2026d667aef863d9e42))
* XML can escape '&' correctly. ([6104912](https://www.github.com/Teletha/sinobu/commit/61049123e7624bd494cebe4999e90c0415e260ea))
* XML#attr and #tagName returns emptry string if the specified ([327d76d](https://www.github.com/Teletha/sinobu/commit/327d76d382b62f544d0eabce1167dc74759ab022))

## [2.3.0](https://www.github.com/Teletha/sinobu/compare/v2.2.2...v2.3.0) (2021-04-18)


### Features

* I#express accepts wildcard [*]. ([d50d9b3](https://www.github.com/Teletha/sinobu/commit/d50d9b30101060f2cd09a78bb339a926729bfa7d))
* I#express can accept mustache-like section. ([440d73f](https://www.github.com/Teletha/sinobu/commit/440d73f03f41478c15eac1377d9833a7d31a151d))
* I#express supports comment section. ([fe0be3e](https://www.github.com/Teletha/sinobu/commit/fe0be3eabe8da3b6eb88baf97191ca03922666b2))
* I#express supports line based block. ([30214a7](https://www.github.com/Teletha/sinobu/commit/30214a728b42427f7b074a849e9e7010e944fbea))


### Bug Fixes

* Delay is too short. ([6d351cb](https://www.github.com/Teletha/sinobu/commit/6d351cbc936b263e4f61f9596d24b8b3d2f3a7d4))
* I#express can accept the nested section. ([0dcbadd](https://www.github.com/Teletha/sinobu/commit/0dcbadde269af4ad7f1579b770809285e458fd40))
* I#express can accepts "this" keyword. ([f10d2d6](https://www.github.com/Teletha/sinobu/commit/f10d2d6bceb483f28f2b83a664d9dad8682d093c))
* Optimize RegEx pattern. ([c3d843c](https://www.github.com/Teletha/sinobu/commit/c3d843c6fd9b098c8bb88b17fa29bbea7d8d7c19))

### [2.2.2](https://www.github.com/Teletha/sinobu/compare/v2.2.1...v2.2.2) (2021-03-28)


### Bug Fixes

* Ignore ClassNotFoundException during classpath scanning. ([27fd7b1](https://www.github.com/Teletha/sinobu/commit/27fd7b1b265a2727209ddd8a970a1049b2bbb640))

### [2.2.1](https://www.github.com/Teletha/sinobu/compare/v2.2.0...v2.2.1) (2021-03-25)


### Bug Fixes

* Delay is too short. ([dc875e9](https://www.github.com/Teletha/sinobu/commit/dc875e98259dfb7cf4030285d68aae7690ecc4ec))
* Delay is too short. ([d29a6a4](https://www.github.com/Teletha/sinobu/commit/d29a6a4603b64ab4f904f83c7ca5d41f0c207418))
* Make codes compilable by javac. ([ffb7e9b](https://www.github.com/Teletha/sinobu/commit/ffb7e9b7141594ead92b5dead911a47bd36f61ef))
* Signal#flatArray is invalid signature. ([8766bb4](https://www.github.com/Teletha/sinobu/commit/8766bb40f8778e073e329ff9c4bdfa1d82bbf332))

## 2.2.0 (2021-03-21)


### Bug Fixes

* Can't resolve outside interface type. ([4aaa403](https://www.github.com/Teletha/sinobu/commit/4aaa403a9ec45434ffc89f522fef2266afe90537))
* Class codec can't resolve primitive types. ([6e5e794](https://www.github.com/Teletha/sinobu/commit/6e5e7949c931ad098f2ba97d487830b3ec49baf6))
* Class loading in jar file is failed. ([bf97ea3](https://www.github.com/Teletha/sinobu/commit/bf97ea3675f352ec7f7058801fca720fc4adff12))
* ClassCodec can't decode array-type class. ([21aa7fa](https://www.github.com/Teletha/sinobu/commit/21aa7fa2e27d4b307671a071f08e95c79b451456))
* ClassCodec can't decode array-type class. ([418d834](https://www.github.com/Teletha/sinobu/commit/418d834ae08dd37cc9715150f612983907a0e85a))
* ClassUtil#getAnnotations collects non-override method's annotations if parent class has same signature private method. ([fe23eda](https://www.github.com/Teletha/sinobu/commit/fe23eda368066bf0dd07cbba72cb00d0d9898c96))
* ClassUtil#getAnnotations contains duplicate annotation. ([fe23eda](https://www.github.com/Teletha/sinobu/commit/fe23eda368066bf0dd07cbba72cb00d0d9898c96))
* ClassUtil#getParameter doesn't compute the correct class agains the overlapped parameter. ([bf78bd8](https://www.github.com/Teletha/sinobu/commit/bf78bd8bd585595236d89bfb8a91ac05782773ac))
* CleanRoom can't create file in not-existing directory. ([cbd0aa4](https://www.github.com/Teletha/sinobu/commit/cbd0aa45f0dde1a81f893198aa8420382905a24e))
* Codec for Locale doesn't use shared instance. ([a45e8f4](https://www.github.com/Teletha/sinobu/commit/a45e8f4ccd61277eeecddc6fa571435356914bec))
* Collection assisted Signal can't dispose. ([b968c71](https://www.github.com/Teletha/sinobu/commit/b968c71857e1cba52db290f155de2a603696a753))
* Crazy HTML crush application. (case sensitive related) ([3330fa1](https://www.github.com/Teletha/sinobu/commit/3330fa1946598a0733c11d266a857ae574b4628b))
* Disposed signal never emit any message. ([6a6332c](https://www.github.com/Teletha/sinobu/commit/6a6332c0f13e9b7e5787a733a8b8b7b0a7d79c7b))
* Doesn't recognize multiple escaped characters. ([097ce0d](https://www.github.com/Teletha/sinobu/commit/097ce0d4aa82d81dca2f007b370f2ee28e4d0e0f))
* End tail whitespace crush application. ([f69909b](https://www.github.com/Teletha/sinobu/commit/f69909baf8d969f73c4172d8cf16cc3c6b287b0f))
* Enum codec can't encode value if it's toString method is ([5605074](https://www.github.com/Teletha/sinobu/commit/5605074569a4ce4edd4c910360f3ef78328c3e12))
* Enum property ignores null value. ([fe48321](https://www.github.com/Teletha/sinobu/commit/fe483219aad89cabdf0b3eb403906a319b3db853))
* Events.NEVER is invalid. ([f6184fe](https://www.github.com/Teletha/sinobu/commit/f6184fecd8479487f81a06f9d9fff8f4a0aa42e2))
* Events#buffer(time, unit) should have side-effect-free updater. ([519f662](https://www.github.com/Teletha/sinobu/commit/519f6622fa2293ce47c630fe6b8db90e09d3d157))
* Events#to(Consumer) is not found. ([be9eca3](https://www.github.com/Teletha/sinobu/commit/be9eca37bae1302fe18247f3523cd0cc041113fe))
* Extension depends on the order of registration. ([9e00e69](https://www.github.com/Teletha/sinobu/commit/9e00e69e37dc6238d9db7ff3bf459eaacd4e490a))
* File observer system doesn't create deamon thred. ([ab24f44](https://www.github.com/Teletha/sinobu/commit/ab24f44dbbc8957946dd913872461441ec0fa4b7))
* File observer system doesn't handle directory exclude pattern properly. ([ab24f44](https://www.github.com/Teletha/sinobu/commit/ab24f44dbbc8957946dd913872461441ec0fa4b7))
* Guaranteed to execute the Signal#delay's complete event last. ([de76f82](https://www.github.com/Teletha/sinobu/commit/de76f82d784f8c00fc2c3fccb4ed7aaf5d536916))
* HttpRequestBuilder doesn't build HttpRequest. ([8d7b423](https://www.github.com/Teletha/sinobu/commit/8d7b423509b4e29967da46361fee58390228d18e))
* I#find may conflict hash. ([dba08a8](https://www.github.com/Teletha/sinobu/commit/dba08a81cb24a32743deaa58bb2e184f3e9ebc1d))
* I#json doesn't accept any Reader input. ([238fdd2](https://www.github.com/Teletha/sinobu/commit/238fdd220c440c76d687ed074a3a8c30a9c59f77))
* I#locate can't resolve escaped character. ([7b16b2b](https://www.github.com/Teletha/sinobu/commit/7b16b2b5e78d584e6c3dfd4404e603bae74e2afb))
* I#locate can't resolve file prorocol. ([f4cd980](https://www.github.com/Teletha/sinobu/commit/f4cd980886698753eb1dfa1d3d5771e30fb2f37f))
* I#make(Class) can't accept interface which has the external-provided Lifestyle. ([99ceb4c](https://www.github.com/Teletha/sinobu/commit/99ceb4c794a9b9b7b4a1e36cfc7b5ef1ca4f69ed))
* I#observe and I#bind throw NPE because they doesn't wipe thier context resources. ([9470a02](https://www.github.com/Teletha/sinobu/commit/9470a02f303cd0161202e2010d9c329f29cda81e))
* I#observe can't apply muliple times. ([7bbdad2](https://www.github.com/Teletha/sinobu/commit/7bbdad2388e5043edcb2d27a21a6eca8d1c1212e))
* I#read must not read transient property from json date. ([5a9ee56](https://www.github.com/Teletha/sinobu/commit/5a9ee5694aadb1dbb3f856cbc809bc06bae8cc85))
* I#walk can't resolve an archive file. ([9d7ac27](https://www.github.com/Teletha/sinobu/commit/9d7ac27dd9dd0eb278fba5394ebb423179b1fd75))
* I#walkDirectory can't recgnize patterns. ([8a36bba](https://www.github.com/Teletha/sinobu/commit/8a36bba95f94e1a562f80dcbf61c1336a29bbb8c))
* I#write method creates file automatically if needed. ([498dc88](https://www.github.com/Teletha/sinobu/commit/498dc88f1c114d5a0bf17e61c7c8d05e1f52612b))
* Ignore empty data on websocket binary. ([444bea0](https://www.github.com/Teletha/sinobu/commit/444bea025b917d666761b93a64c662a57f800837))
* Internal disposer in Events#flatMap affects external events. ([f1e0e6d](https://www.github.com/Teletha/sinobu/commit/f1e0e6d10e5b36db269a3461bf25b6a0299c95a0))
* Invalid encoding name crush application. ([8e6a249](https://www.github.com/Teletha/sinobu/commit/8e6a2492f47f238b9dd466b1c42f5e9995788545))
* Jar entry file name is invalid. ([8173f44](https://www.github.com/Teletha/sinobu/commit/8173f447fd12f63fdbe3b5cd4936c70014b93880))
* Javadoc is missing. ([3b103c1](https://www.github.com/Teletha/sinobu/commit/3b103c131da4b2f6bea943a865e4e8d234a4ea81))
* JSON serializer can handle nulls more properly. ([a73603d](https://www.github.com/Teletha/sinobu/commit/a73603daaf0d778c4b00ff18ddcdc90bbd12fde1))
* Junit has test scope. ([9bfe02c](https://www.github.com/Teletha/sinobu/commit/9bfe02c6798d6a7789e709f42bb0f6884284c573))
* Model accessor throws NPE when some parameter is null. ([07af68e](https://www.github.com/Teletha/sinobu/commit/07af68e4b126bf552f2102b7f5a84ca3a8b1955d))
* Model can't access field property in non-public class. ([6fc8db3](https://www.github.com/Teletha/sinobu/commit/6fc8db3a6399564d841b78ad977ec2f74e39ff0d))
* Model can't resolve the specialized generic type on Variable. ([0595db6](https://www.github.com/Teletha/sinobu/commit/0595db61df2c986467ac14bf9ba6ac0e199361a4))
* Model is not thread-safe. ([6646d5b](https://www.github.com/Teletha/sinobu/commit/6646d5b308a3835d86b7245b27b9daf18afc88da))
* ModelTest fails by class loading order. ([8a6f980](https://www.github.com/Teletha/sinobu/commit/8a6f98043d39e4740f854f5bc4057545f74a15c2))
* Multiple charset detection causes stack over flow. ([ac65de4](https://www.github.com/Teletha/sinobu/commit/ac65de4e5a0a5543cd3dc2029a3d37d6857dae25))
* Path decoder is not found. ([63165f7](https://www.github.com/Teletha/sinobu/commit/63165f7d909738347dbdd6292dcc93dc0566f891))
* PathObserver scan all decendant paths with direct child pattern. ([a673839](https://www.github.com/Teletha/sinobu/commit/a6738398b48947e30a7d8ac60e3dc832efeb3bd2))
* Property inspection is broken because I#recurse is async. ([aaec73f](https://www.github.com/Teletha/sinobu/commit/aaec73fc0b230a47cee1815403abd2b5e4b20202))
* ReusableRule burkes errors in test method. ([048ea2a](https://www.github.com/Teletha/sinobu/commit/048ea2aa9b48acaa604e6c68f84f7257654e12fc))
* ReusableRule throws NPE. ([7229b3c](https://www.github.com/Teletha/sinobu/commit/7229b3c5c3006f6872dfb7c7a8af22e79004cde3))
* SandBox throws IndexOutOfBoundsException when PATH environment value contains sequencial separator character. ([c4a1511](https://www.github.com/Teletha/sinobu/commit/c4a1511ead59a9de81de6762dc69504e94a75fb4))
* Scheduler must have positive core pool. ([add714e](https://www.github.com/Teletha/sinobu/commit/add714e952798c5ac5228ebe45dbce7893d03627))
* Signal error and complete disposes subscription. ([fdd3311](https://www.github.com/Teletha/sinobu/commit/fdd3311dfab2b2e2e28dde3937c435ba118d6016))
* Signal#combine completes immediately if the queue is empty. ([2ceb2f4](https://www.github.com/Teletha/sinobu/commit/2ceb2f4a37d099f2fa4ad814a5c668fe780c759a))
* Signal#delay delays complete event also. ([ad2c327](https://www.github.com/Teletha/sinobu/commit/ad2c3276ee73ad99bdfe42fb9868f91e0db75851))
* Signal#delay failed when complete event without any values. ([0f5db77](https://www.github.com/Teletha/sinobu/commit/0f5db77b597c3d7fbd7c748095bf932fe9ba9505))
* Signal#first disposes the following signal. ([0519471](https://www.github.com/Teletha/sinobu/commit/05194713b61094e6a4207aabd78e3cd7eca7be99))
* Signal#flatMap should ignore complete event from sup process. ([f4101b2](https://www.github.com/Teletha/sinobu/commit/f4101b215369d6001a5858d53807e5a42727b911))
* Signal#infinite can't dispose. ([325ed04](https://www.github.com/Teletha/sinobu/commit/325ed04d6abc02d631c275b428680c3ca173ba13))
* Signal#repeat is broken. ([e40e05b](https://www.github.com/Teletha/sinobu/commit/e40e05b69c6ef9858b83d7aedcd14cd2b9beeeb5))
* Signal#repeatWhen and #retryWhen may throw StackOverflowException. ([fd57182](https://www.github.com/Teletha/sinobu/commit/fd57182a6774f6fb94dc8a3c274a25bc2697697f))
* Signal#share disposes well. ([cf1ee62](https://www.github.com/Teletha/sinobu/commit/cf1ee624ed71eff21e8aee1285976ac2448e1fc5))
* Signal#signal related methods sometimes send COMPLETE event ([e616dfd](https://www.github.com/Teletha/sinobu/commit/e616dfd2e89e55b3ba18576c22437d965409ce8f))
* Signal#startWith(Signal) can't return root disposer. ([5948e2f](https://www.github.com/Teletha/sinobu/commit/5948e2fd24bd9487f8f15e7c7b6b69d2b35ba797))
* Signal#startWith(Supplier) is lazily called. ([ca47a6e](https://www.github.com/Teletha/sinobu/commit/ca47a6e1d8258ca0f9265af372c5bdfbe4be3fae))
* Signal#take and #skip related methods sometimes send COMPLETE event ([c132efd](https://www.github.com/Teletha/sinobu/commit/c132efd8819b273b292c312462e55e1584c52b4a))
* Signal#takeWhile can't drop unconditional data. ([8f69f4e](https://www.github.com/Teletha/sinobu/commit/8f69f4e833c58edeef06ba6d7714e9313b32f800))
* Signale#combine awaits all completions. ([4aecb47](https://www.github.com/Teletha/sinobu/commit/4aecb47e9c8e1a4144fea24e555650157e581a74))
* Sinobu writes invalid JSON format. ([4d6dce3](https://www.github.com/Teletha/sinobu/commit/4d6dce3089ff7c5de168e02fc506f29a2d825918))
* Test for archive. ([c6d7d28](https://www.github.com/Teletha/sinobu/commit/c6d7d285190bd8d940bfd920ca5a8d9513a9084d))
* TestSuite brokes some tests. ([080d806](https://www.github.com/Teletha/sinobu/commit/080d80613e20d19d2f3edab08016a2afbf733ffc))
* The glob pattern "**" ignores other patterns. ([0a53ded](https://www.github.com/Teletha/sinobu/commit/0a53ded8c447943d23a69a0ec36bfa3db70fa803))
* The validator must not call the duration supplier. ([1002d68](https://www.github.com/Teletha/sinobu/commit/1002d68b80cd81ac39ac0c65d5d9b4a92bc3dfd8))
* Transient property on Variable field is ignored. ([d00a7cc](https://www.github.com/Teletha/sinobu/commit/d00a7cc44fb45f9dbf0b672de650aac6d82e25c0))
* TypeVariable must use not "==" operator but "equals" method to check equality. ([bca5acd](https://www.github.com/Teletha/sinobu/commit/bca5acdfb4ec9b6d538054d8dc45226d826d7277))
* URI encodes automatically. ([002849b](https://www.github.com/Teletha/sinobu/commit/002849b4ad42bd66de3095c8f781c03f0da37e53))
* URL class try to access external resource at test phase. ([4622de9](https://www.github.com/Teletha/sinobu/commit/4622de9adaf6f8f0db42c5c1110c849f09b88efa))
* URLConnection requres some user-agent property. ([1b69ede](https://www.github.com/Teletha/sinobu/commit/1b69ede4c2d4c7323940690de7a5f166f1b055ec))
* Visitor can't accept glob pattern. ([545e0f8](https://www.github.com/Teletha/sinobu/commit/545e0f88947b4b42cdc39131a89145d518b6d082))
* Websocket can't handle long-size binary. ([e274300](https://www.github.com/Teletha/sinobu/commit/e2743003534b6157c5e713d352fd99335f0e6d7f))
* When a object has empty list, JSON writes invalid format. ([a0e7e1a](https://www.github.com/Teletha/sinobu/commit/a0e7e1aaea86197bc218b2caff402ec851105047))
* WiseTriConsumer must throw error. ([4fc8907](https://www.github.com/Teletha/sinobu/commit/4fc8907a9113175884af52fa39d6cdd6de94edf7))
* Wrong HTML crush application. (attribute related) ([071f6db](https://www.github.com/Teletha/sinobu/commit/071f6db79e39f32552274a4aacffacf7b44d0b14))
* XML and JSON serialization can't handle escaped linefeed characeter. ([4ef2239](https://www.github.com/Teletha/sinobu/commit/4ef2239d27f2a7b761720e0f85fa480c20efeae8))
* XML can't parse document which has text node in root directly. ([ef49f42](https://www.github.com/Teletha/sinobu/commit/ef49f42039c1858a7375eba40a948c8f887dafbb))
* XML confuses xml like text. ([3cee035](https://www.github.com/Teletha/sinobu/commit/3cee035cc2bd5bf731fa4fad1790ba4c2d06da73))
* XML#last and #first rise error if they are empty. ([dd1ea06](https://www.github.com/Teletha/sinobu/commit/dd1ea06f96ba736584270de36b6a356ff9cdcf34))
* XML#to doesn't flush data properly. ([64d717b](https://www.github.com/Teletha/sinobu/commit/64d717b0193ae787ec2444643eb97d3467c9cd2e))
* XMLScanner can't use extended rule method. ([61bac76](https://www.github.com/Teletha/sinobu/commit/61bac76f8af06b212f9f65b6cc5acbb8c567399d))
* XMLScanner rises StackOverFlowError properly in invalid  method call. ([afab5cc](https://www.github.com/Teletha/sinobu/commit/afab5cca937d9711e5ffd08aeb4369dac4686758))
* XMLWriter outputs invalid CDATA. ([2c84e8e](https://www.github.com/Teletha/sinobu/commit/2c84e8eb3dcfc03c5f9942e6fa11b8958aa7c423))

package cash.p.terminal.core

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.client.SignClient
import cash.p.terminal.BuildConfig
import cash.p.terminal.core.factories.AccountFactory
import cash.p.terminal.core.factories.AdapterFactory
import cash.p.terminal.core.factories.AddressParserFactory
import cash.p.terminal.core.factories.EvmAccountManagerFactory
import cash.p.terminal.core.managers.AccountCleaner
import cash.p.terminal.core.managers.AccountManager
import cash.p.terminal.core.managers.AdapterManager
import cash.p.terminal.core.managers.AppVersionManager
import cash.p.terminal.core.managers.BackgroundStateChangeListener
import cash.p.terminal.core.managers.BackupManager
import cash.p.terminal.core.managers.BalanceHiddenManager
import cash.p.terminal.core.managers.BaseTokenManager
import cash.p.terminal.core.managers.BinanceKitManager
import cash.p.terminal.core.managers.BtcBlockchainManager
import cash.p.terminal.core.managers.CoinManager
import cash.p.terminal.core.managers.ConnectivityManager
import cash.p.terminal.core.managers.CurrencyManager
import cash.p.terminal.core.managers.EvmBlockchainManager
import cash.p.terminal.core.managers.EvmLabelManager
import cash.p.terminal.core.managers.EvmSyncSourceManager
import cash.p.terminal.core.managers.KeyStoreCleaner
import cash.p.terminal.core.managers.LanguageManager
import cash.p.terminal.core.managers.LocalStorageManager
import cash.p.terminal.core.managers.MarketFavoritesManager
import cash.p.terminal.core.managers.MarketKitWrapper
import cash.p.terminal.core.managers.NetworkManager
import cash.p.terminal.core.managers.NftAdapterManager
import cash.p.terminal.core.managers.NftMetadataManager
import cash.p.terminal.core.managers.NftMetadataSyncer
import cash.p.terminal.core.managers.NumberFormatter
import cash.p.terminal.core.managers.RateAppManager
import cash.p.terminal.core.managers.ReleaseNotesManager
import cash.p.terminal.core.managers.RestoreSettingsManager
import cash.p.terminal.core.managers.SolanaKitManager
import cash.p.terminal.core.managers.SolanaRpcSourceManager
import cash.p.terminal.core.managers.SolanaWalletManager
import cash.p.terminal.core.managers.SystemInfoManager
import cash.p.terminal.core.managers.TermsManager
import cash.p.terminal.core.managers.TokenAutoEnableManager
import cash.p.terminal.core.managers.TorManager
import cash.p.terminal.core.managers.TransactionAdapterManager
import cash.p.terminal.core.managers.TronAccountManager
import cash.p.terminal.core.managers.TronKitManager
import cash.p.terminal.core.managers.WalletActivator
import cash.p.terminal.core.managers.WalletManager
import cash.p.terminal.core.managers.WalletStorage
import cash.p.terminal.core.managers.WordsManager
import cash.p.terminal.core.managers.ZcashBirthdayProvider
import cash.p.terminal.core.providers.AppConfigProvider
import cash.p.terminal.core.providers.EvmLabelProvider
import cash.p.terminal.core.providers.FeeRateProvider
import cash.p.terminal.core.providers.FeeTokenProvider
import cash.p.terminal.core.storage.AccountsStorage
import cash.p.terminal.core.storage.AppDatabase
import cash.p.terminal.core.storage.BlockchainSettingsStorage
import cash.p.terminal.core.storage.EnabledWalletsStorage
import cash.p.terminal.core.storage.EvmSyncSourceStorage
import cash.p.terminal.core.storage.NftStorage
import cash.p.terminal.core.storage.RestoreSettingsStorage
import cash.p.terminal.modules.balance.BalanceViewTypeManager
import cash.p.terminal.modules.contacts.ContactsRepository
import cash.p.terminal.modules.keystore.KeyStoreActivity
import cash.p.terminal.modules.launcher.LauncherActivity
import cash.p.terminal.modules.lockscreen.LockScreenActivity
import cash.p.terminal.modules.market.favorites.MarketFavoritesMenuService
import cash.p.terminal.modules.market.topnftcollections.TopNftCollectionsRepository
import cash.p.terminal.modules.market.topnftcollections.TopNftCollectionsViewItemFactory
import cash.p.terminal.modules.market.topplatforms.TopPlatformsRepository
import cash.p.terminal.modules.pin.PinComponent
import cash.p.terminal.modules.profeatures.ProFeaturesAuthorizationManager
import cash.p.terminal.modules.profeatures.storage.ProFeaturesStorage
import cash.p.terminal.modules.theme.ThemeType
import cash.p.terminal.modules.walletconnect.storage.WC1SessionStorage
import cash.p.terminal.modules.walletconnect.storage.WC2SessionStorage
import cash.p.terminal.modules.walletconnect.version1.WC1Manager
import cash.p.terminal.modules.walletconnect.version1.WC1RequestManager
import cash.p.terminal.modules.walletconnect.version1.WC1SessionManager
import cash.p.terminal.modules.walletconnect.version2.WC2Manager
import cash.p.terminal.modules.walletconnect.version2.WC2Service
import cash.p.terminal.modules.walletconnect.version2.WC2SessionManager
import cash.p.terminal.widgets.MarketWidgetManager
import cash.p.terminal.widgets.MarketWidgetRepository
import cash.p.terminal.widgets.MarketWidgetWorker
import io.horizontalsystems.core.BackgroundManager
import io.horizontalsystems.core.CoreApp
import io.horizontalsystems.core.ICoreApp
import io.horizontalsystems.core.security.EncryptionManager
import io.horizontalsystems.core.security.KeyStoreManager
import io.horizontalsystems.ethereumkit.core.EthereumKit
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.plugins.RxJavaPlugins
import java.util.logging.Level
import java.util.logging.Logger
import androidx.work.Configuration as WorkConfiguration

class App : CoreApp(), WorkConfiguration.Provider, ImageLoaderFactory {

    companion object : ICoreApp by CoreApp {

        lateinit var preferences: SharedPreferences
        lateinit var feeRateProvider: FeeRateProvider
        lateinit var localStorage: ILocalStorage
        lateinit var marketStorage: IMarketStorage
        lateinit var torKitManager: ITorManager
        lateinit var restoreSettingsStorage: IRestoreSettingsStorage
        lateinit var currencyManager: CurrencyManager
        lateinit var languageManager: LanguageManager

        lateinit var blockchainSettingsStorage: BlockchainSettingsStorage
        lateinit var evmSyncSourceStorage: EvmSyncSourceStorage
        lateinit var btcBlockchainManager: BtcBlockchainManager
        lateinit var wordsManager: WordsManager
        lateinit var networkManager: INetworkManager
        lateinit var backgroundStateChangeListener: BackgroundStateChangeListener
        lateinit var appConfigProvider: AppConfigProvider
        lateinit var adapterManager: IAdapterManager
        lateinit var transactionAdapterManager: TransactionAdapterManager
        lateinit var walletManager: IWalletManager
        lateinit var walletActivator: WalletActivator
        lateinit var tokenAutoEnableManager: TokenAutoEnableManager
        lateinit var walletStorage: IWalletStorage
        lateinit var accountManager: IAccountManager
        lateinit var accountFactory: IAccountFactory
        lateinit var backupManager: IBackupManager
        lateinit var proFeatureAuthorizationManager: ProFeaturesAuthorizationManager
        lateinit var zcashBirthdayProvider: ZcashBirthdayProvider

        lateinit var connectivityManager: ConnectivityManager
        lateinit var appDatabase: AppDatabase
        lateinit var accountsStorage: IAccountsStorage
        lateinit var enabledWalletsStorage: IEnabledWalletStorage
        lateinit var binanceKitManager: BinanceKitManager
        lateinit var solanaKitManager: SolanaKitManager
        lateinit var tronKitManager: TronKitManager
        lateinit var numberFormatter: IAppNumberFormatter
        lateinit var addressParserFactory: AddressParserFactory
        lateinit var feeCoinProvider: FeeTokenProvider
        lateinit var accountCleaner: IAccountCleaner
        lateinit var rateAppManager: IRateAppManager
        lateinit var coinManager: ICoinManager
        lateinit var wc1SessionStorage: WC1SessionStorage
        lateinit var wc1SessionManager: WC1SessionManager
        lateinit var wc1RequestManager: WC1RequestManager
        lateinit var wc2Service: WC2Service
        lateinit var wc2SessionManager: WC2SessionManager
        lateinit var wc1Manager: WC1Manager
        lateinit var wc2Manager: WC2Manager
        lateinit var termsManager: ITermsManager
        lateinit var marketFavoritesManager: MarketFavoritesManager
        lateinit var marketKit: MarketKitWrapper
        lateinit var releaseNotesManager: ReleaseNotesManager
        lateinit var restoreSettingsManager: RestoreSettingsManager
        lateinit var evmSyncSourceManager: EvmSyncSourceManager
        lateinit var evmBlockchainManager: EvmBlockchainManager
        lateinit var solanaRpcSourceManager: SolanaRpcSourceManager
        lateinit var nftMetadataManager: NftMetadataManager
        lateinit var nftAdapterManager: NftAdapterManager
        lateinit var nftMetadataSyncer: NftMetadataSyncer
        lateinit var evmLabelManager: EvmLabelManager
        lateinit var baseTokenManager: BaseTokenManager
        lateinit var balanceViewTypeManager: BalanceViewTypeManager
        lateinit var balanceHiddenManager: BalanceHiddenManager
        lateinit var marketWidgetManager: MarketWidgetManager
        lateinit var marketWidgetRepository: MarketWidgetRepository
        lateinit var contactsRepository: ContactsRepository
        lateinit var subscriptionManager: SubscriptionManager
    }

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            //Disable logging for lower levels in Release build
            Logger.getLogger("").level = Level.SEVERE
        }

        RxJavaPlugins.setErrorHandler { e: Throwable? ->
            Log.w("RxJava ErrorHandler", e)
        }

        EthereumKit.init()

        instance = this
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val appConfig = AppConfigProvider()
        appConfigProvider = appConfig

        LocalStorageManager(preferences).apply {
            localStorage = this
            pinStorage = this
            thirdKeyboardStorage = this
            marketStorage = this
        }

        torKitManager = TorManager(instance, localStorage)
        subscriptionManager = SubscriptionManager(localStorage)

        marketKit = MarketKitWrapper(
            context = this,
            hsApiBaseUrl = appConfig.marketApiBaseUrl,
            hsApiKey = appConfig.marketApiKey,
            cryptoCompareApiKey = appConfig.cryptoCompareApiKey,
            defiYieldApiKey = appConfig.defiyieldProviderApiKey,
            subscriptionManager = App.subscriptionManager
        )
        marketKit.sync()

        feeRateProvider = FeeRateProvider(appConfigProvider)
        backgroundManager = BackgroundManager(this)

        appDatabase = AppDatabase.getInstance(this)

        blockchainSettingsStorage = BlockchainSettingsStorage(appDatabase)
        evmSyncSourceStorage = EvmSyncSourceStorage(appDatabase)
        evmSyncSourceManager = EvmSyncSourceManager(appConfigProvider, blockchainSettingsStorage, evmSyncSourceStorage)

        btcBlockchainManager = BtcBlockchainManager(blockchainSettingsStorage, marketKit)

        binanceKitManager = BinanceKitManager()

        accountsStorage = AccountsStorage(appDatabase)
        restoreSettingsStorage = RestoreSettingsStorage(appDatabase)

        AppLog.logsDao = appDatabase.logsDao()

        accountCleaner = AccountCleaner()
        accountManager = AccountManager(accountsStorage, accountCleaner)

        val proFeaturesStorage = ProFeaturesStorage(appDatabase)
        proFeatureAuthorizationManager = ProFeaturesAuthorizationManager(proFeaturesStorage, accountManager, appConfigProvider)

        enabledWalletsStorage = EnabledWalletsStorage(appDatabase)
        walletStorage = WalletStorage(marketKit, enabledWalletsStorage)

        walletManager = WalletManager(accountManager, walletStorage)
        coinManager = CoinManager(marketKit, walletManager)

        solanaRpcSourceManager = SolanaRpcSourceManager(blockchainSettingsStorage, marketKit)
        val solanaWalletManager = SolanaWalletManager(walletManager, accountManager, marketKit)
        solanaKitManager = SolanaKitManager(appConfigProvider, solanaRpcSourceManager, solanaWalletManager, backgroundManager)

        tronKitManager = TronKitManager(appConfigProvider, backgroundManager)

        blockchainSettingsStorage = BlockchainSettingsStorage(appDatabase)

        wordsManager = WordsManager(Mnemonic())
        networkManager = NetworkManager()
        accountFactory = AccountFactory(accountManager)
        backupManager = BackupManager(accountManager)


        KeyStoreManager(
            keyAlias = "MASTER_KEY",
            keyStoreCleaner = KeyStoreCleaner(localStorage, accountManager, walletManager),
            logger = AppLogger("key-store")
        ).apply {
            keyStoreManager = this
            keyProvider = this
        }

        encryptionManager = EncryptionManager(keyProvider)

        walletActivator = WalletActivator(walletManager, marketKit)
        tokenAutoEnableManager = TokenAutoEnableManager(appDatabase.tokenAutoEnabledBlockchainDao())

        val evmAccountManagerFactory = EvmAccountManagerFactory(
            accountManager,
            walletManager,
            marketKit,
            tokenAutoEnableManager
        )
        evmBlockchainManager = EvmBlockchainManager(
            backgroundManager,
            evmSyncSourceManager,
            marketKit,
            evmAccountManagerFactory,
        )

        val tronAccountManager = TronAccountManager(
            accountManager,
            walletManager,
            marketKit,
            tronKitManager,
            tokenAutoEnableManager
        )
        tronAccountManager.start()

        systemInfoManager = SystemInfoManager()

        languageManager = LanguageManager()
        currencyManager = CurrencyManager(localStorage, appConfigProvider)
        numberFormatter = NumberFormatter(languageManager)

        connectivityManager = ConnectivityManager(backgroundManager)

        zcashBirthdayProvider = ZcashBirthdayProvider(this)
        restoreSettingsManager = RestoreSettingsManager(restoreSettingsStorage, zcashBirthdayProvider)

        evmLabelManager = EvmLabelManager(
            EvmLabelProvider(),
            appDatabase.evmAddressLabelDao(),
            appDatabase.evmMethodLabelDao(),
            appDatabase.syncerStateDao()
        )

        val adapterFactory = AdapterFactory(instance, btcBlockchainManager, evmBlockchainManager, evmSyncSourceManager, binanceKitManager, solanaKitManager, tronKitManager, backgroundManager, restoreSettingsManager, coinManager, evmLabelManager)
        adapterManager = AdapterManager(walletManager, adapterFactory, btcBlockchainManager, evmBlockchainManager, binanceKitManager, solanaKitManager, tronKitManager)
        transactionAdapterManager = TransactionAdapterManager(adapterManager, adapterFactory)

        feeCoinProvider = FeeTokenProvider(marketKit)

        addressParserFactory = AddressParserFactory()

        pinComponent = PinComponent(
                pinStorage = pinStorage,
                encryptionManager = encryptionManager,
                excludedActivityNames = listOf(
                        KeyStoreActivity::class.java.name,
                        LockScreenActivity::class.java.name,
                        LauncherActivity::class.java.name,
                )
        )

        backgroundStateChangeListener = BackgroundStateChangeListener(systemInfoManager, keyStoreManager, pinComponent).apply {
            backgroundManager.registerListener(this)
        }

        rateAppManager = RateAppManager(walletManager, adapterManager, localStorage)

        wc1SessionStorage = WC1SessionStorage(appDatabase)
        wc1SessionManager = WC1SessionManager(wc1SessionStorage, accountManager, evmSyncSourceManager)
        wc1RequestManager = WC1RequestManager()
        wc1Manager = WC1Manager(accountManager, evmBlockchainManager)
        wc2Manager = WC2Manager(accountManager, evmBlockchainManager)

        termsManager = TermsManager(localStorage)

        marketWidgetManager = MarketWidgetManager()
        marketFavoritesManager = MarketFavoritesManager(appDatabase, marketWidgetManager)

        marketWidgetRepository = MarketWidgetRepository(
            marketKit,
            marketFavoritesManager,
            MarketFavoritesMenuService(localStorage, marketWidgetManager),
            TopNftCollectionsRepository(marketKit),
            TopNftCollectionsViewItemFactory(numberFormatter),
            TopPlatformsRepository(marketKit, currencyManager),
            currencyManager
        )

        releaseNotesManager = ReleaseNotesManager(systemInfoManager, localStorage, appConfigProvider)

        setAppTheme()

        val nftStorage = NftStorage(appDatabase.nftDao(), marketKit)
        nftMetadataManager = NftMetadataManager(marketKit, appConfigProvider, nftStorage)
        nftAdapterManager = NftAdapterManager(walletManager, evmBlockchainManager)
        nftMetadataSyncer = NftMetadataSyncer(nftAdapterManager, nftMetadataManager, nftStorage)

        initializeWalletConnectV2(appConfig)

        wc2Service = WC2Service()
        wc2SessionManager = WC2SessionManager(accountManager, WC2SessionStorage(appDatabase), wc2Service, wc2Manager)

        baseTokenManager = BaseTokenManager(coinManager, localStorage)
        balanceViewTypeManager = BalanceViewTypeManager(localStorage)
        balanceHiddenManager = BalanceHiddenManager(localStorage, backgroundManager)

        contactsRepository = ContactsRepository(marketKit)

        startTasks()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .components {
                add(SvgDecoder.Factory())
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    private fun initializeWalletConnectV2(appConfig: AppConfigProvider) {
        val projectId = appConfig.walletConnectProjectId
        val serverUrl = "wss://${appConfig.walletConnectUrl}?projectId=$projectId"
        val connectionType = ConnectionType.AUTOMATIC
        val appMetaData = Core.Model.AppMetaData(
            name = "Unstoppable",
            description = "",
            url = "unstoppable.money",
            icons = listOf("https://raw.githubusercontent.com/horizontalsystems/HS-Design/master/PressKit/UW-AppIcon-on-light.png"),
            redirect = null,
        )

        CoreClient.initialize(
            metaData = appMetaData,
            relayServerUrl = serverUrl,
            connectionType = connectionType,
            application = this,
            onError = { error ->
                Log.w("AAA", "error", error.throwable)
            },
        )

        val init = Sign.Params.Init(core = CoreClient)
        SignClient.initialize(init) { error ->
            Log.w("AAA", "error", error.throwable)
        }
    }

    private fun setAppTheme() {
        val nightMode = when (localStorage.currentTheme) {
            ThemeType.Light -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeType.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeType.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    override fun getWorkManagerConfiguration(): WorkConfiguration {
        return if (BuildConfig.DEBUG) {
            WorkConfiguration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .build()
        } else {
            WorkConfiguration.Builder()
                .setMinimumLoggingLevel(Log.ERROR)
                .build()
        }
    }

    override fun localizedContext(): Context {
        return localeAwareContext(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(localeAwareContext(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localeAwareContext(this)
    }

    private fun startTasks() {
        Thread {
            rateAppManager.onAppLaunch()
            nftMetadataSyncer.start()
            accountManager.loadAccounts()
            walletManager.loadWallets()
            adapterManager.preloadAdapters()
            accountManager.clearAccounts()

            AppVersionManager(systemInfoManager, localStorage).apply { storeAppVersion() }

            if (MarketWidgetWorker.hasEnabledWidgets(instance)) {
                MarketWidgetWorker.enqueueWork(instance)
            } else {
                MarketWidgetWorker.cancel(instance)
            }

            evmLabelManager.sync()
            contactsRepository.initialize()

        }.start()
    }
}

package malte0811.nbtedit;

import malte0811.nbtedit.client.NBTClipboard;
import malte0811.nbtedit.command.CommandNbtEdit;
import malte0811.nbtedit.nbt.ClientProxy;
import malte0811.nbtedit.nbt.CommonProxy;
import malte0811.nbtedit.network.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(NBTEdit.MODID)
@Mod.EventBusSubscriber
public class NBTEdit {
	public static final String MODID = "nbtedit";
	@SuppressWarnings("WeakerAccess")
	public static final String VERSION = "$version";
	public static final SimpleChannel packetHandler = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, "network"),
		() -> VERSION, s -> true, s -> true);
	public static CommonProxy proxy = new CommonProxy();
	public static Logger logger;

	public NBTEdit() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
		DistExecutor.callWhenOn(Dist.CLIENT, ()->()->System.setProperty("java.awt.headless", "false"));
	}

	public void clientInit(FMLClientSetupEvent event) {
		proxy = new ClientProxy();
	}

	public void init(FMLCommonSetupEvent event) {
		logger = LogManager.getLogger(MODID);
		int id = 0;
		packetHandler.registerMessage(id++, MessageNBTSync.class, MessageNBTSync::toBytes,
			MessageNBTSync::new, MessageNBTSync::onMessage);
		packetHandler.registerMessage(id++, MessagePushNBT.class, MessagePushNBT::toBytes,
			MessagePushNBT::new, MessagePushNBT::onMessage);
		packetHandler.registerMessage(id++, MessageRequestNBT.class, MessageRequestNBT::toBytes,
			MessageRequestNBT::new, MessageRequestNBT::onMessage);
		packetHandler.registerMessage(id++, MessageBlockUpdate.class, MessageBlockUpdate::toBytes,
			MessageBlockUpdate::new, MessageBlockUpdate::onMessage);
		packetHandler.registerMessage(id++, MessageOpenWindow.class, MessageOpenWindow::toBytes,
			MessageOpenWindow::new, MessageOpenWindow::onMessage);
		if (FMLEnvironment.dist == Dist.CLIENT) {
			NBTClipboard.readFromDisc();
			Compat.registerHandlers();
		}
		CommandNbtEdit.registerSerializers();
	}

	@SubscribeEvent
	public static void serverStarted(FMLServerStartingEvent ev) {
		CommandNbtEdit.register(ev.getCommandDispatcher());
	}
}

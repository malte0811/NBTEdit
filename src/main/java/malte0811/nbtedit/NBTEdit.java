package malte0811.nbtedit;

import malte0811.nbtedit.client.ClientEventHandler;
import malte0811.nbtedit.client.NBTClipboard;
import malte0811.nbtedit.command.CommandNbtEdit;
import malte0811.nbtedit.nbt.CommonProxy;
import malte0811.nbtedit.nbt.ClientProxy;
import malte0811.nbtedit.network.MessageBlockUpdate;
import malte0811.nbtedit.network.MessageNBTSync;
import malte0811.nbtedit.network.MessagePushNBT;
import malte0811.nbtedit.network.MessageRequestNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.SidedProvider;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Map;
import java.awt.GraphicsEnvironment;


@Mod(NBTEdit.MODID)
public class NBTEdit {
	public static final String MODID = "nbtedit";
	@SuppressWarnings("WeakerAccess")
	public static final String VERSION = "$version";
	public static final SimpleChannel packetHandler = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, "network"),
			()->VERSION, s->true, s->true);
	public static CommonProxy proxy = new CommonProxy();
	public static Logger logger;

	public NBTEdit() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
	}

	public void clientInit(FMLClientSetupEvent event) {
		proxy = new ClientProxy();
	}

	public void init(FMLCommonSetupEvent event) {
		logger = LogManager.getLogger(MODID);
		//TODO remove this or figure it out
		System.setProperty("java.awt.headless", "false");
		int id = 0;
		packetHandler.registerMessage(id++, MessageNBTSync.class, MessageNBTSync::toBytes,
				MessageNBTSync::new, MessageNBTSync::onMessage);
		packetHandler.registerMessage(id++, MessagePushNBT.class, MessagePushNBT::toBytes,
				MessagePushNBT::new, MessagePushNBT::onMessage);
		packetHandler.registerMessage(id++, MessageRequestNBT.class, MessageRequestNBT::toBytes,
				MessageRequestNBT::new, MessageRequestNBT::onMessage);
		packetHandler.registerMessage(id++, MessageBlockUpdate.class, MessageBlockUpdate::toBytes,
				MessageBlockUpdate::new, MessageBlockUpdate::onMessage);
		if (FMLEnvironment.dist == Dist.CLIENT) {
			NBTClipboard.readFromDisc();
			Compat.registerHandlers();
		}
	}
/*TODO get this working again!
	@NetworkCheckHandler
	@OnlyIn(Dist.CLIENT)
	public boolean checkModLists(Map<String, String> modList, Side side) {

		logger.info(modList+", "+modList.containsKey(MODID));//TODO disable NBTEdit/Proxy provider if NBTEdit isn't installed on the server
		return true;
	}
	*/
}

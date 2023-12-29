package illyena.gilding.mixin.worldgen;

import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@SuppressWarnings("unused")
@Mixin(SimpleStructurePiece.class)
public interface SimpleStructurePieceAccessor {
    @Invoker
    Identifier callGetId();
}

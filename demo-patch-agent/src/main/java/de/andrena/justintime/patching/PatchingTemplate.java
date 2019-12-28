package de.andrena.justintime.patching;

import de.andrena.justintime.application.domain.DateSource;
import de.andrena.justintime.application.fake.Esoterics;
import net.amygdalum.xrayinterface.XRayInterface;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.This;

public class PatchingTemplate {

	//TODO: improve this with @FieldValue and FieldAccessor instead of Xray
	public static boolean badStellarConfiguration(@This Esoterics self, @Argument(0) DateSource date) {
		WithSourceOfKnowledge openSelf = XRayInterface.xray(self).to(WithSourceOfKnowledge.class);
		openSelf.setSourceOfKnowledge((openSelf.getSourceOfKnowledge() + 1) % 6);
		int sourceOfKnowledge = openSelf.getSourceOfKnowledge();
		if (sourceOfKnowledge == 0) {
			return false;
		}
		return date.getDayOfMonth() % sourceOfKnowledge == 0;
	}

	public interface WithSourceOfKnowledge {
		int getSourceOfKnowledge();

		void setSourceOfKnowledge(int sourceOfKnowledge);
	}
}
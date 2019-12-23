package de.andrena.justintime.patching;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public class PatchAgent {

	public static volatile List<PatchAgent> agents = new ArrayList<>();

	private Instrumentation instrumentation;
	private ResettableClassFileTransformer runningTransformer;

	public PatchAgent(Instrumentation instrumentation) {
		this.instrumentation = instrumentation;
		Transformer transformer = (builder, type, loader, module) -> builder
			.method(ElementMatchers.hasMethodName("badStellarConfiguration"))
			.intercept(MethodDelegation.to(PatchingTemplate.class));
		runningTransformer = new AgentBuilder.Default()
			.ignore(ElementMatchers.none())
			.disableClassFormatChanges()
			.with(new AgentBuilder.CircularityLock.Default())
			.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
			.with(new AgentBuilder.Listener.WithErrorsOnly(AgentBuilder.Listener.StreamWriting.toSystemError()))
			.type(ElementMatchers.nameContains("Esoterics"))
			.transform(transformer)
			.installOn(instrumentation);
	}

	private void shutdown() {
		runningTransformer.reset(instrumentation, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
	}

	public static void premain(String arg, Instrumentation instrumentation) {
		agentmain(arg, instrumentation);
	}

	public static void agentmain(String arg, Instrumentation instrumentation) {
		if (arg.equals("detach")) {
			if (agents.isEmpty()) {
				System.out.println("no agent to detach exists");
			}
			for (PatchAgent agent : agents) {
				System.out.println("detaching " + System.identityHashCode(agent));
				agent.shutdown();
			}
			agents.clear();
		} else {
			PatchAgent agent = new PatchAgent(instrumentation);
			System.out.println("attaching " + System.identityHashCode(agent));
			agents.add(agent);
		}
	}
}
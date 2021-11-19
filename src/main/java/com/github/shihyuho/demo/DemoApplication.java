package com.github.shihyuho.demo;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  @Bean
  KubernetesClient kubernetesClient() {
    return new DefaultKubernetesClient();
  }
}

@Slf4j
@Component
@RequiredArgsConstructor
class PodWatcher {

  final KubernetesClient client;

  @EventListener(ApplicationReadyEvent.class)
  void onReady() {
    client.pods().inNamespace("default").watch(new Watcher<>() {
      @Override
      public void eventReceived(Action action, Pod resource) {
        log.info("Event [{}] {}: {} {}", action.name(),
          resource.getMetadata().getName(),
          resource.getStatus().getPhase(),
          resource.getStatus().getConditions().stream().map(PodCondition::getType).collect(Collectors.toList())
        );
      }

      @Override
      public void onClose(WatcherException cause) {
      }
    });
  }
}

@RequiredArgsConstructor
@RequestMapping("/pods")
@RestController
class PodController {

  final KubernetesClient client;

  @GetMapping("/{namespace}")
  List<P> listPods(@PathVariable("namespace") String namespace) {
    return client.pods().inNamespace(namespace).list().getItems().stream().map(P::new).collect(Collectors.toList());
  }

  @PostMapping("/{namespace}/{name}")
  void createPod(@PathVariable("namespace") String namespace, @PathVariable("name") String name) {
    var pod = new PodBuilder()
      .withNewMetadata()
        .withName(name)
      .endMetadata()
      .withNewSpec()
        .withContainers(new ContainerBuilder()
          .withName(name)
          .withImage("busybox")
          .withImagePullPolicy("IfNotPresent")
          .withCommand("sh", "-c", "echo Hello JCConf Taiwan; sleep 2")
          .build())
        .withRestartPolicy("Never")
      .endSpec()
      .build();
    client.pods().inNamespace(namespace).create(pod);
  }

  @DeleteMapping("/{namespace}/{name}")
  Boolean deletePod(@PathVariable("namespace") String namespace, @PathVariable("name") String name) {
    return client.pods().inNamespace(namespace).withName(name).delete();
  }

  @Data
  class P {

    String namespace;
    String name;
    List<E> events;

    public P(Pod pod) {
      this.namespace = pod.getMetadata().getNamespace();
      this.name = pod.getMetadata().getName();
      this.events = client.v1().events().inNamespace(namespace).withInvolvedObject(
        new ObjectReferenceBuilder()
          .withUid(pod.getMetadata().getUid())
          .build()
      ).list().getItems().stream().map(E::new).collect(Collectors.toList());
    }
  }
  
  @Data
  class E {

    String type;
    String reason;
    String message;

    public E(Event event) {
      this.type = event.getType();
      this.message = event.getMessage();
      this.reason = event.getReason();
    }
  }
}

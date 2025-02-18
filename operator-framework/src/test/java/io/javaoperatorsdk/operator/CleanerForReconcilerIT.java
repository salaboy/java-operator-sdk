package io.javaoperatorsdk.operator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.javaoperatorsdk.operator.junit.LocalOperatorExtension;
import io.javaoperatorsdk.operator.sample.cleanerforreconciler.CleanerForReconcilerCustomResource;
import io.javaoperatorsdk.operator.sample.cleanerforreconciler.CleanerForReconcilerTestReconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class CleanerForReconcilerIT {

  public static final String TEST_RESOURCE_NAME = "cleaner-for-reconciler-test1";

  @RegisterExtension
  LocalOperatorExtension operator =
      LocalOperatorExtension.builder().withReconciler(new CleanerForReconcilerTestReconciler())
          .build();


  @Test
  void addsFinalizerAndCallsCleanupIfCleanerImplemented() {
    var testResource = createTestResource();
    operator.create(CleanerForReconcilerCustomResource.class, testResource);

    await().until(() -> !operator.get(CleanerForReconcilerCustomResource.class, TEST_RESOURCE_NAME)
        .getMetadata().getFinalizers().isEmpty());

    operator.delete(CleanerForReconcilerCustomResource.class, testResource);

    await().until(
        () -> operator.get(CleanerForReconcilerCustomResource.class, TEST_RESOURCE_NAME) == null);

    CleanerForReconcilerTestReconciler reconciler =
        (CleanerForReconcilerTestReconciler) operator.getFirstReconciler();
    assertThat(reconciler.getNumberOfExecutions()).isEqualTo(1);
    assertThat(reconciler.getNumberOfCleanupExecutions()).isEqualTo(1);
  }

  private CleanerForReconcilerCustomResource createTestResource() {
    CleanerForReconcilerCustomResource cr = new CleanerForReconcilerCustomResource();
    cr.setMetadata(new ObjectMeta());
    cr.getMetadata().setName(TEST_RESOURCE_NAME);
    return cr;
  }

}

package co.unimagdalena.domine.repositories;

import co.unimagdalena.domine.entities.FareRule;
import co.unimagdalena.domine.entities.DynamicPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FareRuleRepository extends JpaRepository<FareRule,Long> {
    Optional<FareRule> finByRouteIdAndFromStopIdAndToStopId(Long routeId, Long fromStopId, Long toStopId);
    List<FareRule> findByRouteId(Long routeId);

    List<FareRule> findByRouteIdAndDynamicPricing(Long routeId, DynamicPricing DynamicPricing);

    @Modifying
    @Query("UPDATE FareRule fr " +
            "SET fr.dynamicPricing = :status " +
            "WHERE fr.id = :fareRuleId")
    void changeDynamicPricing(@Param("fareRuleId") Long fareRuleId, @Param("status") DynamicPricing status);
}

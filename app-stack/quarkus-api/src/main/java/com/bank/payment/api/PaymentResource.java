package com.bank.payment.api;

import com.bank.payment.domain.PaymentTransaction;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Random;

/**
 * Quando o script chaos-load.sh disparar 5.000 requisições aqui, o código vai:
 *   - Gerar uma latência aleatória (para oscilar o gráfico de tempo de resposta)
 *   - Forçar 5% de erro 500 (para disparar o alerta no Slack!)
 *   - Forçar 10% de erro 400 (cartão recusado).
 *   - Aprovar 85% e salvar no Postgres.
 */
@Path("/api/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    private final Random random = new Random();

    public static class PaymentRequest {
        public Double amount;
    }

    @POST
    @Transactional
    public Response processPayment(PaymentRequest request) throws InterruptedException {
        // Simula latência de rede externa (entre 20ms e 250ms)
        Thread.sleep(random.nextInt(230) + 20);

        double chance = random.nextDouble();

        // 5% de chance de simular queda na operadora do cartão (ERRO 500)
        if (chance < 0.05) {
            throw new RuntimeException("Timeout na comunicação com a Bandeira do Cartão");
        }

        // 10% de chance de dar recusa de saldo (ERRO 400)
        if (chance < 0.15) {
            PaymentTransaction failedTx = new PaymentTransaction(request.amount != null ? request.amount : 0.0, "DECLINED");
            failedTx.persist();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Saldo insuficiente\"}").build();
        }

        // 85% de Sucesso (200 OK) -> Escreve no Postgres
        PaymentTransaction successTx = new PaymentTransaction(
                request.amount != null ? request.amount : 150.0, "APPROVED"
        );
        successTx.persist();

        return Response.ok(successTx).build();
    }

    @GET
    public List<PaymentTransaction> getAllTransactions() {
        // Gera leitura no banco de dados para estressar métricas de SELECT
        return PaymentTransaction.listAll();
    }
}
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  // Handle CORS
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const supabaseClient = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_ANON_KEY') ?? '',
      {
        global: {
          headers: { Authorization: req.headers.get('Authorization')! },
        },
      }
    )

    const { serviceId, amount, phone, userId } = await req.json()

    // Get service details
    const { data: service, error: serviceError } = await supabaseClient
      .from('services')
      .select('*')
      .eq('id', serviceId)
      .single()

    if (serviceError || !service) {
      throw new Error('Service not found')
    }

    // Check user balance
    const { data: balanceData, error: balanceError } = await supabaseClient
      .rpc('get_user_balance', { user_uuid: userId })

    if (balanceError || balanceData < amount) {
      throw new Error('Insufficient balance')
    }

    // Call external API (e-misr example)
    const externalResponse = await callExternalAPI(service, amount, phone)

    // Create transaction
    const { data: transactionId, error: transactionError } = await supabaseClient
      .rpc('create_transaction', {
        p_user_id: userId,
        p_service_id: serviceId,
        p_type: 'payment',
        p_amount: amount,
        p_notes: `Payment for ${service.name}`
      })

    if (transactionError) {
      throw transactionError
    }

    // Update transaction with API response
    await supabaseClient
      .from('transactions')
      .update({
        api_transaction_id: externalResponse.transactionId,
        request_data: { serviceId, amount, phone },
        response_data: externalResponse,
        status: externalResponse.success ? 'completed' : 'failed'
      })
      .eq('id', transactionId)

    return new Response(
      JSON.stringify({
        success: true,
        transactionId,
        externalResponse
      }),
      {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200,
      },
    )

  } catch (error) {
    return new Response(
      JSON.stringify({
        success: false,
        error: error.message
      }),
      {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 400,
      },
    )
  }
})

async function callExternalAPI(service: any, amount: number, phone: string) {
  // Example call to e-misr API
  const response = await fetch('https://api.e-misr.com/json-v2.php', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      AID: '996', // Your API ID
      PWD: 'C7L@-0z9VuX7S', // Your password
      BIN: '9960',
      srv: service.api_service_code || service.code,
      cmob: phone,
      avsa: amount,
      APIID: `TXN_${Date.now()}`,
      do: 'PAY'
    }),
  })

  const data = await response.json()

  return {
    success: data.ST === 'OK',
    transactionId: data.APIID,
    response: data
  }
}
package com.example.app

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.app.databinding.ActivityFiltrosBinding

/**
 * Convertida de FiltrosActivity para Fragment — prova de conceito da
 * migração para Navigation Component (ver docs/PLANO_DESENVOLVIMENTO.md).
 * FiltrosActivity passou a ser só um "host" fino que aloja este Fragment
 * (ver activity_filtros_host.xml e nav_graph_filtros.xml).
 *
 * A lógica é idêntica à FiltrosActivity original — só a forma como o ecrã
 * é hospedado mudou. Reutiliza o mesmo layout (activity_filtros.xml) e a
 * mesma classe de View Binding.
 */
class FiltrosFragment : Fragment() {

    // Padrão recomendado para View Binding em Fragments: a VISTA do Fragment
    // (entre onCreateView e onDestroyView) tem um ciclo de vida mais curto
    // do que o próprio Fragment (que pode continuar na back stack sem vista
    // visível, ex: ao navegar para outro ecrã por cima). Guardar o binding
    // como "_binding" nullable e limpá-lo em onDestroyView evita fugas de
    // memória — ao contrário de uma Activity, onde o binding vive tanto
    // tempo quanto a própria Activity e nunca precisa disto.
    // Documentação oficial: https://developer.android.com/topic/libraries/view-binding#fragments
    private var _binding: ActivityFiltrosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityFiltrosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val maior = binding.bMaior
        val menor = binding.bMenor
        val tudo = binding.bTudo

        val esperas = binding.bEsperas
        val montaria = binding.bMontarias
        val tordos = binding.bTordos
        val rolas = binding.bRolas
        val dias = binding.bDias

        // Antes: intent.getIntExtra(...) na própria Activity.
        // Agora: o Fragment não tem Intent próprio, lê o da Activity anfitriã
        // (requireActivity().intent) — continua a ser o mesmo Intent que
        // LoginActivity/HomeActivity/etc. enviam para FiltrosActivity, nada
        // mudou do lado de quem chama este ecrã.
        val marca = requireActivity().intent.getIntExtra(EXTRA_MESSAGE, -1)

        if (marca == 1) {
            val novaMarca = 0
            val intent = Intent(requireContext(), FiltrosActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, novaMarca)
            }
            startActivity(intent)
        }

        rolas.visibility = View.INVISIBLE
        tordos.visibility = View.INVISIBLE
        montaria.visibility = View.INVISIBLE
        esperas.visibility = View.INVISIBLE
        dias.visibility = View.INVISIBLE

        if (marca == 0) {
            maior.setOnClickListener {
                rolas.visibility = View.INVISIBLE
                dias.visibility = View.INVISIBLE
                tordos.visibility = View.INVISIBLE
                montaria.visibility = View.VISIBLE
                esperas.visibility = View.VISIBLE

                esperas.setOnClickListener {
                    val filtro = "esperas"
                    val intentEsperas = Intent(requireContext(), HomeActivity::class.java).apply {
                        putExtra(EXTRA_MESSAGE, filtro)
                    }
                    startActivity(intentEsperas)
                    Log.d("filtro", "g : $filtro")
                }

                montaria.setOnClickListener {
                    val filtro = "montaria"
                    val intentMontaria = Intent(requireContext(), HomeActivity::class.java).apply {
                        putExtra(EXTRA_MESSAGE, filtro)
                    }
                    startActivity(intentMontaria)
                    Log.d("filtro", "g : $filtro")
                }
            }

            menor.setOnClickListener {
                val marcaAtual = requireActivity().intent.getIntExtra(EXTRA_MESSAGE, 0)

                if (marcaAtual == 1) {
                    startActivity(Intent(requireContext(), FiltrosActivity::class.java))
                }

                rolas.visibility = View.VISIBLE
                tordos.visibility = View.VISIBLE
                dias.visibility = View.VISIBLE
                montaria.visibility = View.INVISIBLE
                esperas.visibility = View.INVISIBLE

                tordos.setOnClickListener {
                    val filtro = "tordos"
                    val intentTordos = Intent(requireContext(), HomeActivity::class.java).apply {
                        putExtra(EXTRA_MESSAGE, filtro)
                    }
                    startActivity(intentTordos)
                }
                rolas.setOnClickListener {
                    val filtro = "rolas"
                    val intentRolas = Intent(requireContext(), HomeActivity::class.java).apply {
                        putExtra(EXTRA_MESSAGE, filtro)
                    }
                    startActivity(intentRolas)
                }
                dias.setOnClickListener {
                    val filtro = "dias"
                    val intentDias = Intent(requireContext(), HomeActivity::class.java).apply {
                        putExtra(EXTRA_MESSAGE, filtro)
                    }
                    startActivity(intentDias)
                }
            }

            tudo.setOnClickListener {
                val marcaAtual = requireActivity().intent.getIntExtra(EXTRA_MESSAGE, 0)

                if (marcaAtual == 1) {
                    startActivity(Intent(requireContext(), FiltrosActivity::class.java))
                }

                rolas.visibility = View.INVISIBLE
                tordos.visibility = View.INVISIBLE
                dias.visibility = View.INVISIBLE
                montaria.visibility = View.INVISIBLE
                esperas.visibility = View.INVISIBLE

                val filtro = "tudo"
                val intentTudo = Intent(requireContext(), HomeActivity::class.java).apply {
                    putExtra(EXTRA_MESSAGE, filtro)
                }
                startActivity(intentTudo)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Ver comentário em _binding acima — este é o passo que, se
        // esquecido, causa a fuga de memória mais comum ao usar View
        // Binding em Fragments.
        _binding = null
    }
}

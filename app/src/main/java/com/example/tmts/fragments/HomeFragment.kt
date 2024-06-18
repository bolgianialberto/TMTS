package com.example.tmts.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import com.example.tmts.R

class HomeFragment : Fragment() {
    private lateinit var btnFilm: Button
    private lateinit var btnSerie: Button
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        btnFilm = view.findViewById(R.id.btn_film);
        btnSerie = view.findViewById(R.id.btn_serie);

        toggleButtonColor(btnFilm)

        btnFilm.setOnClickListener {
            toggleButtonColor(btnFilm)
            replaceFragment(MovieHomeFragment())}
        btnSerie.setOnClickListener {
            toggleButtonColor(btnSerie)
            replaceFragment(SerieHomeFragment())
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Imposta il frammento di default come FilmHomeFragment all'avvio
        replaceFragment(MovieHomeFragment())
    }

    private fun toggleButtonColor(button: Button) {
        if (button.isSelected) return

        btnFilm.isSelected = false
        btnSerie.isSelected = false

        button.isSelected = true
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selectedColor))
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        val nonClickedButton = if (button.id == R.id.btn_film) btnSerie else btnFilm
        nonClickedButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.unselectedColor))
        nonClickedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_home_fragment, fragment)
        fragmentTransaction.commit()
    }
}